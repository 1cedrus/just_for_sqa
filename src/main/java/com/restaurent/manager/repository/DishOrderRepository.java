package com.restaurent.manager.repository;

import com.restaurent.manager.entity.DishOrder;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DishOrderRepository extends JpaRepository<DishOrder,Long> {
    List<DishOrder> findDishOrderByOrder_Id(Long orderId);
    List<DishOrder> findDishOrderByOrder_Id(Long orderId, Pageable pageable);
    int countByOrder_Id(Long orderId);
    List<DishOrder> findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(Long orderId, DISH_ORDER_STATE state, LocalDateTime startTime, LocalDateTime endTime);

}
