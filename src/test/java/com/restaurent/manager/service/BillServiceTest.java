package com.restaurent.manager.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.restaurent.manager.service.impl.BillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.restaurent.manager.dto.request.BillRequest;
import com.restaurent.manager.dto.response.BillResponse;
import com.restaurent.manager.dto.response.order.*;
import com.restaurent.manager.entity.Bill;
import com.restaurent.manager.entity.Customer;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.TableRestaurant;
import com.restaurent.manager.entity.Vat;
import com.restaurent.manager.enums.MethodPayment;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.BillMapper;
import com.restaurent.manager.repository.BillRepository;
import com.restaurent.manager.repository.CustomerRepository;
import com.restaurent.manager.repository.TableRestaurantRepository;
import com.restaurent.manager.service.IOrderService;
import com.restaurent.manager.service.IRestaurantService;
import com.restaurent.manager.service.ITableRestaurantService;

class BillServiceTest {

    @InjectMocks
    private BillService billService;

    @Mock
    private IOrderService orderService;

    @Mock
    private BillMapper billMapper;

    @Mock
    private BillRepository billRepository;

    @Mock
    private ITableRestaurantService tableRestaurantService;

    @Mock
    private TableRestaurantRepository tableRestaurantRepository;

    @Mock
    private IRestaurantService restaurantService;

    @Mock
    private CustomerRepository customerRepository;

    private Order mockOrder;
    private Customer mockCustomer;
    private TableRestaurant mockTable;
    private Restaurant mockRestaurant;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setCurrentPoint(100);
        mockCustomer.setTotalPoint(200);
        mockRestaurant = new Restaurant();
        mockRestaurant.setMoneyToPoint(10);
        mockTable = new TableRestaurant();
        mockTable.setId(1L);
        mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setCustomer(mockCustomer);
        mockOrder.setTableRestaurant(mockTable);
        mockOrder.setRestaurant(mockRestaurant);
        orderService = mock(IOrderService.class);
        billMapper = mock(BillMapper.class);
        billRepository = mock(BillRepository.class);
        tableRestaurantService = mock(ITableRestaurantService.class);
        tableRestaurantRepository = mock(TableRestaurantRepository.class);
        restaurantService = mock(IRestaurantService.class);
        customerRepository = mock(CustomerRepository.class);
        pageable = PageRequest.of(0, 10);

        billService = new BillService(
                orderService,
                billMapper,
                billRepository,
                tableRestaurantService,
                tableRestaurantRepository,
                restaurantService,
                customerRepository
        );
    }

    @Test
    void testCreateBillWithoutUsingPoints() {
        BillRequest request = new BillRequest();
        request.setTotal(1000);
        request.setPoints(0);
        request.setMethodPayment(MethodPayment.CASH);

        Bill billEntity = new Bill();
        billEntity.setTotal(1000);
        billEntity.setMethodPayment(MethodPayment.CASH);

        BillResponse expectedResponse = new BillResponse();
        expectedResponse.setTotal(1000);

        when(orderService.findOrderById(1L)).thenReturn(mockOrder);
        when(tableRestaurantService.findById(mockTable.getId())).thenReturn(mockTable);
        when(billMapper.toBill(request)).thenReturn(billEntity);
        when(billRepository.save(any())).thenReturn(billEntity);
        when(billMapper.toBillResponse(billEntity)).thenReturn(expectedResponse);

        BillResponse result = billService.createBill(1L, request);

        assertEquals(1000, result.getTotal());
        assertEquals(100 + 100, mockCustomer.getCurrentPoint());
        assertEquals(200 + 100, mockCustomer.getTotalPoint());
        verify(customerRepository).save(mockCustomer);
    }

    @Test
    void testCreateBillUsingValidPoints() {
        BillRequest request = new BillRequest();
        request.setTotal(500);
        request.setPoints(50);
        request.setMethodPayment(MethodPayment.CASH);

        Bill billEntity = new Bill();
        billEntity.setTotal(500);
        billEntity.setMethodPayment(MethodPayment.CASH);

        BillResponse expectedResponse = new BillResponse();
        expectedResponse.setTotal(500);

        when(orderService.findOrderById(1L)).thenReturn(mockOrder);
        when(tableRestaurantService.findById(mockTable.getId())).thenReturn(mockTable);
        when(billMapper.toBill(request)).thenReturn(billEntity);
        when(billRepository.save(any())).thenReturn(billEntity);
        when(billMapper.toBillResponse(billEntity)).thenReturn(expectedResponse);

        BillResponse result = billService.createBill(1L, request);

        assertEquals(500, result.getTotal());
        assertEquals(50, mockCustomer.getCurrentPoint());
        verify(customerRepository).save(mockCustomer);
    }

    @Test
    void testCreateBillUsingInvalidPoints_ThrowsException() {
        BillRequest request = new BillRequest();
        request.setTotal(1000);
        request.setPoints(200); // > 100 hiện tại
        request.setMethodPayment(MethodPayment.CASH);

        when(orderService.findOrderById(1L)).thenReturn(mockOrder);
        when(tableRestaurantService.findById(mockTable.getId())).thenReturn(mockTable);
        when(billMapper.toBill(request)).thenReturn(new Bill());

        AppException exception = assertThrows(AppException.class, () -> {
            billService.createBill(1L, request);
        });

        assertEquals(ErrorCode.POINT_INVALID, exception.getErrorCode());
    }

    @Test
    void testGetBillsByRestaurantId_ReturnsBillResponses_WhenBillsExist() {
        Long restaurantId = 1L;

        Bill bill1 = new Bill();
        bill1.setId(101L);
        bill1.setTotal(100.0);
        bill1.setDateCreated(LocalDateTime.now());

        Bill bill2 = new Bill();
        bill2.setId(102L);
        bill2.setTotal(200.0);
        bill2.setDateCreated(LocalDateTime.now());

        BillResponse response1 = new BillResponse();
        response1.setId(101L);
        response1.setTotal(100.0);

        BillResponse response2 = new BillResponse();
        response2.setId(102L);
        response2.setTotal(200.0);

        when(billRepository.findByOrder_Restaurant_Id(restaurantId, pageable)).thenReturn(List.of(bill1, bill2));
        when(billMapper.toBillResponse(bill1)).thenReturn(response1);
        when(billMapper.toBillResponse(bill2)).thenReturn(response2);

        List<BillResponse> result = billService.getBillsByRestaurantId(restaurantId, pageable);

        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(102L, result.get(1).getId());
    }

    @Test
    void testGetBillsByRestaurantId_ReturnsEmptyList_WhenNoBillsFound() {
        Long restaurantId = 2L;

        when(billRepository.findByOrder_Restaurant_Id(restaurantId, pageable)).thenReturn(Collections.emptyList());

        List<BillResponse> result = billService.getBillsByRestaurantId(restaurantId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(billRepository, times(1)).findByOrder_Restaurant_Id(restaurantId, pageable);
        verify(billMapper, never()).toBillResponse(any());
    }

    @Test
    void testGetDetailBillByBillId_ReturnsDishOrderResponses_WhenBillExists() {
        Long billId = 1L;
        Long orderId = 10L;

        Order order = new Order();
        order.setId(orderId);

        Bill bill = new Bill();
        bill.setId(billId);
        bill.setOrder(order);

        DishOrderResponse response1 = DishOrderResponse.builder().id(100L).build();
        DishOrderResponse response2 = DishOrderResponse.builder().id(200L).build();

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(orderService.findDishByOrderId(orderId, pageable)).thenReturn(List.of(response1, response2));

        List<DishOrderResponse> result = billService.getDetailBillByBillId(billId, pageable);

        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(200L, result.get(1).getId());

        verify(billRepository).findById(billId);
        verify(orderService).findDishByOrderId(orderId, pageable);
    }

    @Test
    void testGetDetailBillByBillId_ReturnsEmptyList_WhenNoDishOrderResponses() {
        Long billId = 2L;
        Long orderId = 20L;

        Order order = new Order();
        order.setId(orderId);

        Bill bill = new Bill();
        bill.setId(billId);
        bill.setOrder(order);

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(orderService.findDishByOrderId(orderId, pageable)).thenReturn(Collections.emptyList());

        List<DishOrderResponse> result = billService.getDetailBillByBillId(billId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(billRepository).findById(billId);
        verify(orderService).findDishByOrderId(orderId, pageable);
    }

    @Test
    void testGetDetailBillByBillId_ThrowsException_WhenBillNotFound() {
        Long billId = 3L;

        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                billService.getDetailBillByBillId(billId, pageable)
        );

        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());

        verify(billRepository).findById(billId);
        verify(orderService, never()).findDishByOrderId(any(), any());
    }

    @Test
    void testFindBillById_WhenBillExists_ReturnsBill() {
        // given
        Long billId = 1L;
        Bill bill = new Bill();
        bill.setId(billId);

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // when
        Bill result = billService.findBillById(billId);

        // then
        assertNotNull(result);
        assertEquals(billId, result.getId());
        verify(billRepository).findById(billId);
    }

    @Test
    void testFindBillById_WhenBillDoesNotExist_ThrowsException() {
        // given
        Long billId = 99L;
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        // when & then
        AppException exception = assertThrows(AppException.class, () -> {
            billService.findBillById(billId);
        });

        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
        verify(billRepository).findById(billId);
    }

    @Test
    void testGetProfitRestaurantByIdAndDate_WhenBillsExist_ShouldReturnRoundedTotal() {
        // given
        Long restaurantId = 1L;
        LocalDateTime date = LocalDateTime.of(2024, 4, 10, 0, 0);
        Date sqlDate = Date.valueOf(date.toLocalDate());

        Bill bill1 = new Bill();
        bill1.setTotal(123.45);
        Bill bill2 = new Bill();
        bill2.setTotal(300.55);

        when(billRepository.findByDateCreated(restaurantId, sqlDate))
                .thenReturn(List.of(bill1, bill2));

        // when
        double result = billService.getProfitRestaurantByIdAndDate(restaurantId, date);

        // then
        assertEquals(Math.round(123.45 + 300.55), result);
        verify(billRepository).findByDateCreated(restaurantId, sqlDate);
    }

    @Test
    void testGetProfitRestaurantByIdAndDate_WhenNoBillsExist_ShouldReturnZero() {
        // given
        Long restaurantId = 2L;
        LocalDateTime date = LocalDateTime.of(2024, 4, 10, 0, 0);
        Date sqlDate = Date.valueOf(date.toLocalDate());

        when(billRepository.findByDateCreated(restaurantId, sqlDate))
                .thenReturn(Collections.emptyList());

        // when
        double result = billService.getProfitRestaurantByIdAndDate(restaurantId, date);

        // then
        assertEquals(0, result);
        verify(billRepository).findByDateCreated(restaurantId, sqlDate);
    }

    @Test
    void testGetProfitBetween_WhenBillsExist_ShouldReturnRoundedTotal() {
        // Arrange
        Long restaurantId = 1L;
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 30, 23, 59);

        Bill bill1 = new Bill();
        bill1.setTotal(99.49);
        Bill bill2 = new Bill();
        bill2.setTotal(200.50);

        when(billRepository.findByDateCreatedBetween(restaurantId, start, end))
                .thenReturn(List.of(bill1, bill2));

        // Act
        double result = billService.getProfitRestaurantByIdAndDateBetween(restaurantId, start, end);

        // Assert
        assertEquals(Math.round(99.49 + 200.50), result);
        verify(billRepository).findByDateCreatedBetween(restaurantId, start, end);
    }

    @Test
    void testGetProfitBetween_WhenNoBillsExist_ShouldReturnZero() {
        // Arrange
        Long restaurantId = 2L;
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 31, 23, 59);

        when(billRepository.findByDateCreatedBetween(restaurantId, start, end))
                .thenReturn(Collections.emptyList());

        // Act
        double result = billService.getProfitRestaurantByIdAndDateBetween(restaurantId, start, end);

        // Assert
        assertEquals(0, result);
        verify(billRepository).findByDateCreatedBetween(restaurantId, start, end);
    }

    @Test
    void testGetVatValueWithValidVat() {
        // Arrange
        Long restaurantId = 1L;
        LocalDateTime date = LocalDateTime.of(2024, 4, 10, 12, 0);
        Date sqlDate = Date.valueOf(date.toLocalDate());

        Bill bill1 = new Bill(); bill1.setTotal(200);
        Bill bill2 = new Bill(); bill2.setTotal(300);

        Vat vat = new Vat(); vat.setTaxValue(10); // 10%

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(true);
        restaurant.setVat(vat);

        when(billRepository.findByDateCreated(restaurantId, sqlDate))
                .thenReturn(List.of(bill1, bill2));
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        // Act
        double result = billService.getVatValueForRestaurantCurrent(restaurantId, date);

        // VAT = total * (vat / (100 + vat)) = 500 * (10 / 110) = 45.45 → round = 45
        assertEquals(Math.round(500 * (10.0 / 110)), result);
    }

    @Test
    void testGetVatValueWithVatDisabled() {
        Long restaurantId = 2L;
        LocalDateTime date = LocalDateTime.now();
        Date sqlDate = Date.valueOf(date.toLocalDate());

        Bill bill = new Bill(); bill.setTotal(100);

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(false); // VAT disabled
        restaurant.setVat(new Vat());

        when(billRepository.findByDateCreated(restaurantId, sqlDate)).thenReturn(List.of(bill));
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        double result = billService.getVatValueForRestaurantCurrent(restaurantId, date);
        assertEquals(0, result);
    }

    @Test
    void testGetVatValueWithNullVatObject() {
        Long restaurantId = 3L;
        LocalDateTime date = LocalDateTime.now();
        Date sqlDate = Date.valueOf(date.toLocalDate());

        Bill bill = new Bill(); bill.setTotal(100);

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(true);
        restaurant.setVat(null); // No VAT object

        when(billRepository.findByDateCreated(restaurantId, sqlDate)).thenReturn(List.of(bill));
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        double result = billService.getVatValueForRestaurantCurrent(restaurantId, date);
        assertEquals(0, result);
    }

    @Test
    void testGetVatValueWithNoBills() {
        Long restaurantId = 4L;
        LocalDateTime date = LocalDateTime.now();
        Date sqlDate = Date.valueOf(date.toLocalDate());

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(true);
        restaurant.setVat(new Vat());

        when(billRepository.findByDateCreated(restaurantId, sqlDate)).thenReturn(Collections.emptyList());
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        double result = billService.getVatValueForRestaurantCurrent(restaurantId, date);
        assertEquals(0, result);
    }

    @Test
    void testGetVatValueBetween_WithValidVat() {
        Long restaurantId = 1L;
        LocalDateTime start = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 10, 23, 59);

        Bill bill1 = new Bill(); bill1.setTotal(200);
        Bill bill2 = new Bill(); bill2.setTotal(300);

        Vat vat = new Vat(); vat.setTaxValue(10); // 10%

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(true);
        restaurant.setVat(vat);

        when(billRepository.findByDateCreatedBetween(restaurantId, start, end))
                .thenReturn(List.of(bill1, bill2));
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        double expectedVat = Math.round(500 * (10.0 / 110)); // total * vat/(100+vat)
        double result = billService.getVatValueForRestaurantBetween(restaurantId, start, end);

        assertEquals(expectedVat, result);
    }

    @Test
    void testGetVatValueBetween_VatDisabled() {
        Long restaurantId = 2L;
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        Bill bill = new Bill(); bill.setTotal(150);

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(false); // VAT disabled
        restaurant.setVat(new Vat());

        when(billRepository.findByDateCreatedBetween(restaurantId, start, end))
                .thenReturn(List.of(bill));
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        double result = billService.getVatValueForRestaurantBetween(restaurantId, start, end);
        assertEquals(0, result);
    }

    @Test
    void testGetVatValueBetween_NullVatObject() {
        Long restaurantId = 3L;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now();

        Bill bill = new Bill(); bill.setTotal(100);

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(true);
        restaurant.setVat(null); // Null VAT

        when(billRepository.findByDateCreatedBetween(restaurantId, start, end))
                .thenReturn(List.of(bill));
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        double result = billService.getVatValueForRestaurantBetween(restaurantId, start, end);
        assertEquals(0, result);
    }

    @Test
    void testGetVatValueBetween_NoBills() {
        Long restaurantId = 4L;
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 5, 0, 0);

        Restaurant restaurant = new Restaurant();
        restaurant.setVatActive(true);
        restaurant.setVat(new Vat());

        when(billRepository.findByDateCreatedBetween(restaurantId, start, end))
                .thenReturn(Collections.emptyList());
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);

        double result = billService.getVatValueForRestaurantBetween(restaurantId, start, end);
        assertEquals(0, result);
    }



    @Test
    void testGetTotalValue_WithValidBills() {
        Long restaurantId = 1L;
        String startTime = "10:00:00";
        String endTime = "14:00:00";

        Bill bill1 = new Bill(); bill1.setTotal(200.0);
        Bill bill2 = new Bill(); bill2.setTotal(300.0);

        when(billRepository.findByTimeBetweenAndCurrentDate(restaurantId, startTime, endTime))
                .thenReturn(List.of(bill1, bill2));

        double result = billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId, startTime, endTime);
        assertEquals(500.0, result);
    }

    @Test
    void testGetTotalValue_NoBillsFound() {
        Long restaurantId = 2L;
        String startTime = "08:00:00";
        String endTime = "09:00:00";

        when(billRepository.findByTimeBetweenAndCurrentDate(restaurantId, startTime, endTime))
                .thenReturn(Collections.emptyList());

        double result = billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId, startTime, endTime);
        assertEquals(0.0, result);
    }

    @Test
    void testGetTotalValue_WithZeroTotalBill() {
        Long restaurantId = 3L;
        String startTime = "12:00:00";
        String endTime = "15:00:00";

        Bill bill1 = new Bill(); bill1.setTotal(0.0);
        Bill bill2 = new Bill(); bill2.setTotal(100.0);

        when(billRepository.findByTimeBetweenAndCurrentDate(restaurantId, startTime, endTime))
                .thenReturn(List.of(bill1, bill2));

        double result = billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId, startTime, endTime);
        assertEquals(100.0, result);
    }

    @Test
    void testGetTotalValue_WithNegativeTotal() {
        Long restaurantId = 4L;
        String startTime = "17:00:00";
        String endTime = "20:00:00";

        Bill bill1 = new Bill(); bill1.setTotal(150.0);
        Bill bill2 = new Bill(); bill2.setTotal(-50.0); // refund or canceled

        when(billRepository.findByTimeBetweenAndCurrentDate(restaurantId, startTime, endTime))
                .thenReturn(List.of(bill1, bill2));

        double result = billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId, startTime, endTime);
        assertEquals(100.0, result);
    }
}

