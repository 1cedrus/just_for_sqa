package com.restaurent.manager.repository;

import com.restaurent.manager.entity.DishOrder;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DishOrderRepositoryTest {

    @Autowired
    private DishOrderRepository dishOrderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private DishOrder sampleDishOrder;
    private Order sampleOrder;

    @BeforeEach
    public void setUp() {
        Order setupOrder = new Order();

        DishOrder setupDishOrder = new DishOrder();
        setupDishOrder.setOrder(setupOrder);
        setupDishOrder.setStatus(DISH_ORDER_STATE.WAITING); // Giả sử DISH_ORDER_STATE là enum
        setupDishOrder.setOrderDate(LocalDateTime.of(2025, 4, 9, 10, 0)); // Ngày 2025-04-09 10:00

        sampleOrder = entityManager.persist(setupOrder);
        sampleDishOrder = entityManager.persist(setupDishOrder);
        entityManager.flush();
    }

    // Test findDishOrderByOrder_Id (không phân trang) - Thành công
    @Test
    public void testFindDishOrderByOrderId_Success() {
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_Id(sampleOrder.getId());
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish order với orderId " + sampleOrder.getId());
        assertEquals(DISH_ORDER_STATE.WAITING, result.get(0).getStatus(), "Status phải là WAITING");
    }

    // Test findDishOrderByOrder_Id (không phân trang) - Thất bại
    @Test
    public void testFindDishOrderByOrderId_NoMatch() {
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_Id(999L);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish order nào với orderId 999");
    }

    // Test findDishOrderByOrder_Id (có phân trang) - Thành công
    @Test
    public void testFindDishOrderByOrderIdWithPageable_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_Id(sampleOrder.getId(), pageable);
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish order với orderId " + sampleOrder.getId());
        assertEquals(DISH_ORDER_STATE.WAITING, result.get(0).getStatus(), "Status phải là WAITING");
    }

    // Test findDishOrderByOrder_Id (có phân trang) - Thất bại
    @Test
    public void testFindDishOrderByOrderIdWithPageable_NoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_Id(999L, pageable);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish order nào với orderId 999");
    }

    // Test countByOrder_Id - Thành công
    @Test
    public void testCountByOrderId_Success() {
        int count = dishOrderRepository.countByOrder_Id(sampleOrder.getId());
        assertEquals(1, count, "Phải đếm được 1 dish order với orderId " + sampleOrder.getId());
    }

    // Test countByOrder_Id - Thất bại
    @Test
    public void testCountByOrderId_NoMatch() {
        int count = dishOrderRepository.countByOrder_Id(999L);
        assertEquals(0, count, "Phải đếm được 0 dish order với orderId 999");
    }

    // Test findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate - Thành công
    @Test
    public void testFindDishOrderByOrderIdAndStatusAndOrderDateBetween_Success() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 9, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 9, 23, 59);
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(
                sampleOrder.getId(), DISH_ORDER_STATE.WAITING, startTime, endTime);
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish order với orderId " + sampleOrder.getId());
        assertEquals(DISH_ORDER_STATE.WAITING, result.get(0).getStatus(), "Status phải là WAITING");
    }

    // Test findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate - Thất bại (không khớp status)
    @Test
    public void testFindDishOrderByOrderIdAndStatusAndOrderDateBetween_NoMatchStatus() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 9, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 9, 23, 59);
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(
                sampleOrder.getId(), DISH_ORDER_STATE.PREPARE, startTime, endTime);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish order nào với status COMPLETED");
    }

    // Test findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate - Thất bại (ngoài khoảng thời gian)
    @Test
    public void testFindDishOrderByOrderIdAndStatusAndOrderDateBetween_NoMatchDate() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 10, 23, 59);
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(
                sampleOrder.getId(), DISH_ORDER_STATE.WAITING, startTime, endTime);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish order nào ngoài khoảng thời gian");
    }

    // Test findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate - Thất bại (không khớp orderId)
    @Test
    public void testFindDishOrderByOrderIdAndStatusAndOrderDateBetween_NoMatchOrderId() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 9, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 9, 23, 59);
        List<DishOrder> result = dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(
                999L, DISH_ORDER_STATE.WAITING, startTime, endTime);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish order nào với orderId 999");
    }
}
