package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.entity.DishOrder;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishOrderMapper;
import com.restaurent.manager.repository.DishOrderRepository;
import com.restaurent.manager.service.IOrderService;
import com.restaurent.manager.service.impl.DishOrderService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Tích hợp Mockito với JUnit 5
public class DishOrderServiceTest {

    // Mock các dependency của DishOrderService
    @Mock
    private DishOrderRepository dishOrderRepository;

    @Mock
    private DishOrderMapper dishOrderMapper;

    @Mock
    private IOrderService orderService;

    // Inject các mock vào DishOrderService
    @InjectMocks
    private DishOrderService dishOrderService;

    // Biến mẫu để tái sử dụng trong các test case
    private DishOrder sampleDishOrder;
    private DishOrderResponse sampleResponse;
    private Order sampleOrder;
    private LocalDateTime startOfDay;
    private LocalDateTime endOfDay;

    /**
     * Thiết lập dữ liệu mẫu trước mỗi test case.
     * Khởi tạo các đối tượng mẫu và reset trạng thái mock.
     */
    @BeforeEach
    public void setUp() {
        // Tạo DishOrder mẫu
        sampleDishOrder = new DishOrder();
        sampleDishOrder.setId(1L);
        sampleDishOrder.setStatus(DISH_ORDER_STATE.WAITING);
        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleDishOrder.setOrder(sampleOrder);

        // Tạo DishOrderResponse mẫu
        sampleResponse = DishOrderResponse.builder().id(1L).status(String.valueOf(DISH_ORDER_STATE.WAITING)).build();

        // Tạo thời gian mẫu cho ngày hiện tại
        LocalDate today = LocalDate.now();
        startOfDay = today.atStartOfDay();
        endOfDay = today.atTime(23, 59, 59);
    }

    /**
     * Test changeStatusDishOrderById khi thay đổi trạng thái thành công.
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testChangeStatusDishOrderById_Success() {
        // Tạo DishOrder mẫu sau thay đổi trạng thái
        DishOrder changeStatusOrder = new DishOrder();
        changeStatusOrder.setId(1L);
        changeStatusOrder.setStatus(DISH_ORDER_STATE.PREPARE);
        changeStatusOrder.setOrder(sampleOrder);

        // Tạo DishOrderResponse mẫu sau khi thay đổi trạng thái
        DishOrderResponse changeStatusOrderResponse = DishOrderResponse.builder()
                .id(1L)
                .status(String.valueOf(DISH_ORDER_STATE.PREPARE))
                .build();

        // Mock tìm thấy DishOrder
        when(dishOrderRepository.findById(1L)).thenReturn(Optional.of(sampleDishOrder));

        // Mock lưu DishOrder sau khi thay đổi trạng thái
        when(dishOrderRepository.save(sampleDishOrder)).thenReturn(changeStatusOrder);
        // Mock ánh xạ sang response
        when(dishOrderMapper.toDishOrderResponse(changeStatusOrder)).thenReturn(changeStatusOrderResponse);

        // Gọi phương thức cần test
        DishOrderResponse result = dishOrderService.changeStatusDishOrderById(1L, DISH_ORDER_STATE.PREPARE);

        // Xác nhận kết quả
        assertNotNull(result, "Response không được null");
        assertEquals(String.valueOf(DISH_ORDER_STATE.PREPARE), result.getStatus(), "Trạng thái phải là PREPARE");
        // Xác nhận trạng thái đã được cập nhật
        verify(dishOrderRepository).save(argThat(dishOrder -> dishOrder.getStatus() == DISH_ORDER_STATE.PREPARE));
    }

    /**
     * Test changeStatusDishOrderById khi không tìm thấy DishOrder (ném exception).
     * Kiểm tra nhánh thất bại của findById trong phương thức.
     */
    @Test
    public void testChangeStatusDishOrderById_NotFound() {
        // Mock không tìm thấy DishOrder
        when(dishOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            dishOrderService.changeStatusDishOrderById(1L, DISH_ORDER_STATE.PREPARE);
        });

        // Xác nhận lỗi là NOT_EXIST
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode(), "Lỗi phải là NOT_EXIST");
        // Xác nhận không gọi save khi không tìm thấy
        verify(dishOrderRepository, never()).save(any());
    }

    /**
     * Test khi cả WAITING và PREPARE đều có dữ liệu.
     * Nhánh: waitingOrder not empty && prepareOrder not empty.
     */
    @Test
    public void testFindDishOrderWaitingByAndRestaurantId_BothStates() {
        // Mock danh sách Order
        List<Order> orders = List.of(sampleOrder);
        when(orderService.findOrderByRestaurantId(1L)).thenReturn(orders);

        // Mock danh sách DishOrder cho WAITING
        List<DishOrder> waitingOrders = List.of(sampleDishOrder);
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(waitingOrders);

        // Mock danh sách DishOrder cho PREPARE
        DishOrder prepareOrder = new DishOrder();
        prepareOrder.setId(2L);
        prepareOrder.setStatus(DISH_ORDER_STATE.PREPARE);
        prepareOrder.setOrder(sampleOrder);
        List<DishOrder> prepareOrders = List.of(prepareOrder);
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.PREPARE, startOfDay, endOfDay))
                .thenReturn(prepareOrders);

        // Mock ánh xạ
        when(dishOrderMapper.toDishOrderResponse(sampleDishOrder)).thenReturn(sampleResponse);
        DishOrderResponse prepareResponse = DishOrderResponse.builder()
                .id(2L)
                .status(String.valueOf(DISH_ORDER_STATE.PREPARE))
                .build();
        when(dishOrderMapper.toDishOrderResponse(prepareOrder)).thenReturn(prepareResponse);

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderWaitingByAndRestaurantId(1L);

        // Xác nhận kết quả
        assertEquals(2, result.size(), "Phải tìm thấy 2 dish order (WAITING và PREPARE)");
        assertTrue(result.stream().anyMatch(r -> r.getStatus().equals(String.valueOf(DISH_ORDER_STATE.WAITING))), "Phải có WAITING");
        assertTrue(result.stream().anyMatch(r -> r.getStatus().equals(String.valueOf(DISH_ORDER_STATE.PREPARE))), "Phải có PREPARE");
    }

    /**
     * Test khi chỉ WAITING có dữ liệu, PREPARE là empty.
     * Nhánh: waitingOrder is not empty && prepareOrder is empty.
     */
    @Test
    public void testFindDishOrderWaitingByAndRestaurantId_OnlyWaiting() {
        // Mock danh sách Order
        List<Order> orders = List.of(sampleOrder);
        when(orderService.findOrderByRestaurantId(1L)).thenReturn(orders);

        // Mock danh sách DishOrder cho WAITING
        List<DishOrder> waitingOrders = List.of(sampleDishOrder);
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(waitingOrders);

        // Mock danh sách DishOrder cho PREPARE trả về danh sách rỗng
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.PREPARE, startOfDay, endOfDay))
                .thenReturn(new ArrayList<>());

        // Mock ánh xạ
        when(dishOrderMapper.toDishOrderResponse(sampleDishOrder)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderWaitingByAndRestaurantId(1L);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish order (chỉ WAITING)");
        assertEquals(String.valueOf(DISH_ORDER_STATE.WAITING), result.get(0).getStatus(), "Trạng thái phải là WAITING");
    }
    /**
     * Test khi chỉ PREPARE có dữ liệu, WAITING là empty.
     * Nhánh: waitingOrder is empty && prepareOrder is not empty.
     */
    @Test
    public void testFindDishOrderWaitingByAndRestaurantId_OnlyPrepare() {
        // Mock danh sách Order
        List<Order> orders = List.of(sampleOrder);
        when(orderService.findOrderByRestaurantId(1L)).thenReturn(orders);

        // Mock danh sách DishOrder cho WAITING trả về danh sách rỗng
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(new ArrayList<>());

        // Mock danh sách DishOrder cho PREPARE
        DishOrder prepareOrder = new DishOrder();
        prepareOrder.setId(2L);
        prepareOrder.setStatus(DISH_ORDER_STATE.PREPARE);
        prepareOrder.setOrder(sampleOrder);
        List<DishOrder> prepareOrders = List.of(prepareOrder);
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.PREPARE, startOfDay, endOfDay))
                .thenReturn(prepareOrders);

        // Mock ánh xạ
        DishOrderResponse prepareResponse = DishOrderResponse.builder()
                .id(2L)
                .status(String.valueOf(DISH_ORDER_STATE.PREPARE))
                .build();
        when(dishOrderMapper.toDishOrderResponse(prepareOrder)).thenReturn(prepareResponse);

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderWaitingByAndRestaurantId(1L);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish order (chỉ PREPARE)");
        assertEquals(String.valueOf(DISH_ORDER_STATE.PREPARE), result.get(0).getStatus(), "Trạng thái phải là PREPARE");
    }

    /**
     * Test khi cả WAITING và PREPARE đều empty.
     * Nhánh: waitingOrder is empty && prepareOrder is empty.
     */
    @Test
    public void testFindDishOrderWaitingByAndRestaurantId_NoOrders() {
        // Mock danh sách Order
        List<Order> orders = List.of(sampleOrder);
        when(orderService.findOrderByRestaurantId(1L)).thenReturn(orders);

        // Mock danh sách DishOrder cho WAITING và PREPARE đều trả về danh sách rỗng
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(new ArrayList<>());
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.PREPARE, startOfDay, endOfDay))
                .thenReturn(new ArrayList<>());

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderWaitingByAndRestaurantId(1L);

        // Xác nhận kết quả
        assertTrue(result.isEmpty(), "Danh sách phải trống khi không có WAITING và PREPARE");
    }

    /**
     * Test findById khi tìm thấy DishOrder.
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testFindById_Found() {
        // Mock tìm thấy DishOrder
        when(dishOrderRepository.findById(1L)).thenReturn(Optional.of(sampleDishOrder));

        // Gọi phương thức cần test
        DishOrder result = dishOrderService.findById(1L);

        // Xác nhận kết quả
        assertNotNull(result, "DishOrder không được null");
        assertEquals(1L, result.getId(), "ID phải là 1");
    }

    /**
     * Test findById khi không tìm thấy DishOrder (ném exception).
     * Kiểm tra nhánh thất bại của phương thức.
     */
    @Test
    public void testFindById_NotFound() {
        // Mock không tìm thấy DishOrder
        when(dishOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            dishOrderService.findById(1L);
        });

        // Xác nhận lỗi là NOT_EXIST
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode(), "Lỗi phải là NOT_EXIST");
    }

    /**
     * Test findDishOrderByOrderId khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công với phân trang.
     */
    @Test
    public void testFindDishOrderByOrderId() {
        // Tạo Pageable mẫu
        Pageable pageable = PageRequest.of(0, 10);
        // Mock danh sách DishOrder
        List<DishOrder> dishOrders = List.of(sampleDishOrder);
        when(dishOrderRepository.findDishOrderByOrder_Id(1L, pageable)).thenReturn(dishOrders);
        // Mock đếm số lượng
        when(dishOrderRepository.countByOrder_Id(1L)).thenReturn(1);
        // Mock ánh xạ
        when(dishOrderMapper.toDishOrderResponse(sampleDishOrder)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        PagingResult<DishOrderResponse> result = dishOrderService.findDishOrderByOrderId(1L, pageable);

        // Xác nhận kết quả
        assertEquals(1, result.getResults().size(), "Phải tìm thấy 1 dish order");
        assertEquals(1, result.getTotalItems(), "Tổng số item phải là 1");
        assertEquals(1L, result.getResults().get(0).getId(), "ID phải là 1");
    }

    /**
     * Test findDishOrderByOrderIdAndStatus khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testFindDishOrderByOrderIdAndStatus_Found() {
        // Mock danh sách DishOrder
        List<DishOrder> dishOrders = List.of(sampleDishOrder);
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(dishOrders);
        // Mock ánh xạ
        when(dishOrderMapper.toDishOrderResponse(sampleDishOrder)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderByOrderIdAndStatus(1L, DISH_ORDER_STATE.WAITING);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish order");
        assertEquals(String.valueOf(DISH_ORDER_STATE.WAITING), result.get(0).getStatus(), "Trạng thái phải là WAITING");
    }

    /**
     * Test findDishOrderByOrderIdAndStatus khi không tìm thấy dữ liệu.
     * Kiểm tra nhánh thất bại của phương thức.
     */
    @Test
    public void testFindDishOrderByOrderIdAndStatus_NotFound() {
        // Mock danh sách DishOrder trống
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(new ArrayList<>());

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderByOrderIdAndStatus(1L, DISH_ORDER_STATE.WAITING);

        // Xác nhận kết quả
        assertTrue(result.isEmpty(), "Danh sách phải trống khi không tìm thấy");
    }

    /**
     * Test findDishOrderByRestaurantIdAndState khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công khi có order và dish order.
     */
    @Test
    public void testFindDishOrderByRestaurantIdAndState_Found() {
        // Mock danh sách Order
        List<Order> orders = List.of(sampleOrder);
        when(orderService.findOrderByRestaurantId(1L)).thenReturn(orders);
        // Mock danh sách DishOrder
        List<DishOrder> dishOrders = List.of(sampleDishOrder);
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(dishOrders);
        // Mock ánh xạ
        when(dishOrderMapper.toDishOrderResponse(sampleDishOrder)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderByRestaurantIdAndState(1L, DISH_ORDER_STATE.WAITING);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish order");
        assertEquals(String.valueOf(DISH_ORDER_STATE.WAITING), result.get(0).getStatus(), "Trạng thái phải là WAITING");
    }

    /**
     * Test findDishOrderByRestaurantIdAndState khi không có order.
     * Kiểm tra nhánh thất bại khi danh sách order trống.
     */
    @Test
    public void testFindDishOrderByRestaurantIdAndState_NoOrders() {
        // Mock danh sách Order trống
        when(orderService.findOrderByRestaurantId(1L)).thenReturn(new ArrayList<>());

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderByRestaurantIdAndState(1L, DISH_ORDER_STATE.WAITING);

        // Xác nhận kết quả
        assertTrue(result.isEmpty(), "Danh sách phải trống khi không có order");
    }

    /**
     * Test findDishOrderByRestaurantIdAndState khi có order nhưng không có dish order.
     * Kiểm tra nhánh thất bại khi danh sách dish order trống.
     */
    @Test
    public void testFindDishOrderByRestaurantIdAndState_NoDishOrders() {
        // Mock danh sách Order
        List<Order> orders = List.of(sampleOrder);
        when(orderService.findOrderByRestaurantId(1L)).thenReturn(orders);
        // Mock danh sách DishOrder trống
        when(dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(1L, DISH_ORDER_STATE.WAITING, startOfDay, endOfDay))
                .thenReturn(new ArrayList<>());

        // Gọi phương thức cần test
        List<DishOrderResponse> result = dishOrderService.findDishOrderByRestaurantIdAndState(1L, DISH_ORDER_STATE.WAITING);

        // Xác nhận kết quả
        assertTrue(result.isEmpty(), "Danh sách phải trống khi không có dish order");
    }
}
