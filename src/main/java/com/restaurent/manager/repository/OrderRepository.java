package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findOrderByRestaurant_Id(Long restaurantId);
}
