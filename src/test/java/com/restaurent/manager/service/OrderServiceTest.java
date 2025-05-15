package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.request.order.OrderRequest;
import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.dto.response.CustomerResponse;
import com.restaurent.manager.dto.response.DishResponse;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.dto.response.order.OrderResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishOrderMapper;
import com.restaurent.manager.mapper.OrderMapper;
import com.restaurent.manager.repository.DishOrderRepository;
import com.restaurent.manager.repository.OrderRepository;
import com.restaurent.manager.repository.TableRestaurantRepository;
import com.restaurent.manager.service.*;
import com.restaurent.manager.service.impl.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho OrderService, đảm bảo độ phủ nhánh cấp 2.
 * Bao gồm tất cả các trường hợp throw exception.
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private IEmployeeService employeeService;

    @Mock
    private ITableRestaurantService tableRestaurantService;

    @Mock
    private IRestaurantService restaurantService;

    @Mock
    private TableRestaurantRepository tableRestaurantRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private IDishService dishService;

    @Mock
    private DishOrderMapper dishOrderMapper;

    @Mock
    private DishOrderRepository dishOrderRepository;

    @Mock
    private IComboService comboService;

    @Mock
    private ICustomerService customerService;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;
    private TableRestaurant sampleTable;
    private Employee sampleEmployee;
    private Restaurant sampleRestaurant;
    private Customer sampleCustomer;
    private Dish sampleDish;
    private Combo sampleCombo;
    private OrderRequest sampleOrderRequest;
    private DishOrderRequest sampleDishOrderRequest;

    @BeforeEach
    public void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setOrderDate(LocalDate.now());

        sampleTable = new TableRestaurant();
        sampleTable.setId(1L);

        sampleEmployee = new Employee();
        sampleEmployee.setId(1L);

        sampleRestaurant = new Restaurant();
        sampleRestaurant.setId(1L);
        sampleRestaurant.setVatActive(false);

        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setPhoneNumber("123456789");

        sampleDish = new Dish();
        sampleDish.setId(1L);
        sampleDish.setPrice(100.0);

        sampleCombo = new Combo();
        sampleCombo.setId(1L);
        sampleCombo.setPrice(200.0);

        sampleOrderRequest = new OrderRequest();
        sampleOrderRequest.setTableId(1L);
        sampleOrderRequest.setEmployeeId(1L);
        sampleOrderRequest.setRestaurantId(1L);
        CustomerResponse customerResponse = new CustomerResponse();
        customerResponse.setPhoneNumber("123456789");
        sampleOrderRequest.setCustomerResponse(customerResponse);
        sampleDishOrderRequest = DishOrderRequest.builder().dishId(1L).quantity(2).build();
    }
    /**
     * Test createOrder thành công.
     */
    // TestcaseID: OS-1
    @Test
    public void testCreateOrder_Success() {
        when(tableRestaurantService.findById(1L)).thenReturn(sampleTable);
        when(customerService.findCustomerByPhoneNumber("123456789", 1L)).thenReturn(sampleCustomer);
        when(restaurantService.getRestaurantById(1L)).thenReturn(sampleRestaurant);
        when(employeeService.findEmployeeById(1L)).thenReturn(sampleEmployee);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(tableRestaurantRepository.save(sampleTable)).thenReturn(sampleTable);
        when(orderMapper.toOrderResponse(sampleOrder)).thenReturn(new OrderResponse());

        OrderResponse result = orderService.createOrder(sampleOrderRequest);

        assertNotNull(result);
        verify(tableRestaurantRepository).save(sampleTable);
    }

    /**
     * Test addDishToOrder với dishId (dishOrders null).
     * Kiểm tra nhánh dishId != null và dishOrders == null.
     */
    // TestcaseID: OS-2
    @Test
    public void testAddDishToOrder_WithDishId_DishOrdersNull() {
        DishOrder dishOrder = new DishOrder();
        dishOrder.setId(1L);
        DishResponse sampleDishResponse = new DishResponse();
        sampleDishResponse.setId(1L);
        DishOrderResponse dishOrderResponse = DishOrderResponse.builder().dish(sampleDishResponse).build();

        sampleOrder.setDishOrders(null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(dishOrderMapper.toDishOrder(sampleDishOrderRequest)).thenReturn(dishOrder);
        when(dishService.findByDishId(1L)).thenReturn(sampleDish);
        when(dishOrderRepository.save(dishOrder)).thenReturn(dishOrder);
        when(dishOrderMapper.toDishOrderResponse(dishOrder)).thenReturn(dishOrderResponse);

        List<DishOrderResponse> result = orderService.addDishToOrder(1L, List.of(sampleDishOrderRequest));

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getDish());
        verify(orderRepository).save(sampleOrder);
    }

    /**
     * Test addDishToOrder với comboId (dishOrders null).
     * Kiểm tra nhánh dishId == null và dishOrders == null.
     */
    // TestcaseID: OS-3
    @Test
    public void testAddDishToOrder_WithComboId_DishOrdersNull() {
        DishOrderRequest comboRequest = DishOrderRequest.builder().build();
        comboRequest.setComboId(1L);
        comboRequest.setQuantity(1);
        DishOrder dishOrder = new DishOrder();
        dishOrder.setId(2L);
        ComboResponse sampleComboResponse = new ComboResponse();
        sampleComboResponse.setId(1L);
        DishOrderResponse dishOrderResponse = DishOrderResponse.builder().combo(sampleComboResponse).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(dishOrderMapper.toDishOrder(comboRequest)).thenReturn(dishOrder);
        when(comboService.findComboById(1L)).thenReturn(sampleCombo);
        when(dishOrderRepository.save(dishOrder)).thenReturn(dishOrder);
        when(dishOrderMapper.toDishOrderResponse(dishOrder)).thenReturn(dishOrderResponse);

        List<DishOrderResponse> result = orderService.addDishToOrder(1L, List.of(comboRequest));

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getCombo());
        verify(orderRepository).save(sampleOrder);
    }

    /**
     * Test addDishToOrder với dishId (dishOrders không null).
     * Kiểm tra nhánh dishId != null và dishOrders != null.
     */
    // TestcaseID: OS-4
    @Test
    public void testAddDishToOrder_WithDishId_DishOrdersNotNull() {
        DishOrder dishOrder = new DishOrder();
        dishOrder.setId(1L);
        sampleOrder.setDishOrders(new HashSet<>());
        DishResponse sampleDishResponse = new DishResponse();
        sampleDishResponse.setId(1L);
        DishOrderResponse dishOrderResponse = DishOrderResponse.builder().dish(sampleDishResponse).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(dishOrderMapper.toDishOrder(sampleDishOrderRequest)).thenReturn(dishOrder);
        when(dishService.findByDishId(1L)).thenReturn(sampleDish);
        when(dishOrderRepository.save(dishOrder)).thenReturn(dishOrder);
        when(dishOrderMapper.toDishOrderResponse(dishOrder)).thenReturn(dishOrderResponse);

        List<DishOrderResponse> result = orderService.addDishToOrder(1L, List.of(sampleDishOrderRequest));

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getDish());
        verify(orderRepository).save(sampleOrder);
    }

    /**
     * Test addDishToOrder với comboId (dishOrders không null).
     * Kiểm tra nhánh dishId == null và dishOrders != null.
     */
    // TestcaseID: OS-5
    @Test
    public void testAddDishToOrder_WithComboId_DishOrdersNotNull() {
        DishOrderRequest comboRequest = DishOrderRequest.builder().build();
        comboRequest.setComboId(1L);
        comboRequest.setQuantity(1);
        DishOrder dishOrder = new DishOrder();
        dishOrder.setId(2L);
        sampleOrder.setDishOrders(new HashSet<>());
        ComboResponse sampleComboResponse = new ComboResponse();
        sampleComboResponse.setId(1L);
        DishOrderResponse dishOrderResponse = DishOrderResponse.builder().combo(sampleComboResponse).build();


        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(dishOrderMapper.toDishOrder(comboRequest)).thenReturn(dishOrder);
        when(comboService.findComboById(1L)).thenReturn(sampleCombo);
        when(dishOrderRepository.save(dishOrder)).thenReturn(dishOrder);
        when(dishOrderMapper.toDishOrderResponse(dishOrder)).thenReturn(dishOrderResponse);

        List<DishOrderResponse> result = orderService.addDishToOrder(1L, List.of(comboRequest));

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getCombo());
        verify(orderRepository).save(sampleOrder);
    }

    /**
     * Test findDishByOrderId (không phân trang).
     */
    // TestcaseID: OS-6
    @Test
    public void testFindDishByOrderId() {
        DishOrder dishOrder = new DishOrder();
        when(dishOrderRepository.findDishOrderByOrder_Id(1L)).thenReturn(List.of(dishOrder));
        when(dishOrderMapper.toDishOrderResponse(dishOrder)).thenReturn(DishOrderResponse.builder().build());

        List<DishOrderResponse> result = orderService.findDishByOrderId(1L);

        assertEquals(1, result.size());
    }

    /**
     * Test findDishByOrderId (có phân trang).
     */
    // TestcaseID: OS-7
    @Test
    public void testFindDishByOrderId_WithPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        DishOrder dishOrder = new DishOrder();
        when(dishOrderRepository.findDishOrderByOrder_Id(1L, pageable)).thenReturn(List.of(dishOrder));
        when(dishOrderMapper.toDishOrderResponse(dishOrder)).thenReturn(DishOrderResponse.builder().build());

        List<DishOrderResponse> result = orderService.findDishByOrderId(1L, pageable);

        assertEquals(1, result.size());
    }

    /**
     * Test findOrderById khi tìm thấy.
     */
    // TestcaseID: OS-8
    @Test
    public void testFindOrderById_Found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        Order result = orderService.findOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test findOrderById khi không tìm thấy.
     * Kiểm tra nhánh orElseThrow.
     */
    // TestcaseID: OS-9
    @Test
    public void testFindOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            orderService.findOrderById(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    /**
     * Test findOrderByRestaurantId.
     */
    // TestcaseID: OS-10
    @Test
    public void testFindOrderByRestaurantId() {
        when(orderRepository.findOrderByRestaurant_Id(1L)).thenReturn(List.of(sampleOrder));

        List<Order> result = orderService.findOrderByRestaurantId(1L);

        assertEquals(1, result.size());
    }

    /**
     * Test findOrderByTableId khi có order hiện tại.
     * Kiểm tra nhánh orderCurrent != null.
     */
    // TestcaseID: OS-11
    @Test
    public void testFindOrderByTableId_OrderExists() {
        OrderResponse sampleOrderResponse = new OrderResponse();
        sampleOrderResponse.setId(1L);

        sampleTable.setOrderCurrent(1L);
        when(tableRestaurantService.findById(1L)).thenReturn(sampleTable);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.toOrderResponse(sampleOrder)).thenReturn(sampleOrderResponse);

        OrderResponse result = orderService.findOrderByTableId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test findOrderByTableId khi không có order hiện tại.
     * Kiểm tra nhánh orderCurrent == null.
     */
    // TestcaseID: OS-12
    @Test
    public void testFindOrderByTableId_NoOrder() {
        sampleTable.setOrderCurrent(null);
        when(tableRestaurantService.findById(1L)).thenReturn(sampleTable);

        OrderResponse result = orderService.findOrderByTableId(1L);

        assertNull(result);
    }

    /**
     * Test findOrderAndConvertDTOByOrderId với dish, không VAT.
     * Kiểm tra nhánh dish != null, isVatActive = false.
     */
    // TestcaseID: OS-13
    @Test
    public void testFindOrderAndConvertDTOByOrderId_WithDish_NoVat() {
        DishOrder dishOrder = new DishOrder();
        dishOrder.setDish(sampleDish);
        dishOrder.setQuantity(2);
        dishOrder.setStatus(DISH_ORDER_STATE.WAITING);
        sampleOrder.setDishOrders(Set.of(dishOrder));
        sampleOrder.setRestaurant(sampleRestaurant);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.toOrderResponse(sampleOrder)).thenReturn(new OrderResponse());
        when(restaurantService.getRestaurantById(1L)).thenReturn(sampleRestaurant);

        OrderResponse result = orderService.findOrderAndConvertDTOByOrderId(1L);

        assertEquals(200.0, result.getTotalMoney()); // 100 * 2
        assertEquals(2, result.getTotalDish());
    }

    /**
     * Test findOrderAndConvertDTOByOrderId với combo, có VAT.
     * Kiểm tra nhánh dish == null, isVatActive = true, vat != null.
     */
    // TestcaseID: OS-14
    @Test
    public void testFindOrderAndConvertDTOByOrderId_WithCombo_WithVat() {
        DishOrder dishOrder = new DishOrder();
        dishOrder.setCombo(sampleCombo);
        dishOrder.setQuantity(1);
        dishOrder.setStatus(DISH_ORDER_STATE.WAITING);
        sampleOrder.setDishOrders(Set.of(dishOrder));
        sampleRestaurant.setVatActive(true);
        Vat vat = new Vat();
        vat.setTaxValue(10.0F);
        sampleRestaurant.setVat(vat);
        sampleOrder.setRestaurant(sampleRestaurant);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.toOrderResponse(sampleOrder)).thenReturn(new OrderResponse());
        when(restaurantService.getRestaurantById(1L)).thenReturn(sampleRestaurant);

        OrderResponse result = orderService.findOrderAndConvertDTOByOrderId(1L);

        assertEquals(220.0, result.getTotalMoney()); // 200 + 10% VAT
        assertEquals(1, result.getTotalDish());
    }

    /**
     * Test findOrderAndConvertDTOByOrderId khi trạng thái DECLINE.
     * Kiểm tra nhánh status == DECLINE.
     */
    // TestcaseID: OS-15
    @Test
    public void testFindOrderAndConvertDTOByOrderId_DeclineStatus() {
        DishOrder dishOrder = new DishOrder();
        dishOrder.setDish(sampleDish);
        dishOrder.setQuantity(1);
        dishOrder.setStatus(DISH_ORDER_STATE.DECLINE);
        sampleOrder.setDishOrders(Set.of(dishOrder));
        sampleOrder.setRestaurant(sampleRestaurant);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.toOrderResponse(sampleOrder)).thenReturn(new OrderResponse());
        when(restaurantService.getRestaurantById(1L)).thenReturn(sampleRestaurant);

        OrderResponse result = orderService.findOrderAndConvertDTOByOrderId(1L);

        assertEquals(0.0, result.getTotalMoney());
        assertEquals(0, result.getTotalDish());
    }

    /**
     * Test findOrderAndConvertDTOByOrderId khi VAT active nhưng vat null.
     * Kiểm tra nhánh isVatActive = true, vat == null.
     */
    // TestcaseID: OS-16
    @Test
    public void testFindOrderAndConvertDTOByOrderId_VatActive_VatNull() {
        DishOrder dishOrder = new DishOrder();
        dishOrder.setDish(sampleDish);
        dishOrder.setQuantity(1);
        dishOrder.setStatus(DISH_ORDER_STATE.WAITING);
        sampleOrder.setDishOrders(Set.of(dishOrder));
        sampleRestaurant.setVatActive(true);
        sampleRestaurant.setVat(null); // VAT null
        sampleOrder.setRestaurant(sampleRestaurant);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.toOrderResponse(sampleOrder)).thenReturn(new OrderResponse());
        when(restaurantService.getRestaurantById(1L)).thenReturn(sampleRestaurant);

        OrderResponse result = orderService.findOrderAndConvertDTOByOrderId(1L);

        assertEquals(100.0, result.getTotalMoney()); // 100 * 1, không cộng VAT vì vat == null
        assertEquals(1, result.getTotalDish());
    }

    /**
     * Test createOrder với tham số entity thành công.
     */
    // TestcaseID: OS-17
    @Test
    public void testCreateOrder_WithEntities() {
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(tableRestaurantRepository.save(sampleTable)).thenReturn(sampleTable);

        Long result = orderService.createOrder(sampleCustomer, sampleEmployee, sampleTable, sampleRestaurant);

        assertEquals(1L, result);
        verify(tableRestaurantRepository).save(sampleTable);
    }
}
