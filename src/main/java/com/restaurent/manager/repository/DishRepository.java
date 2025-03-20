package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Dish;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    // Các phương thức tùy chỉnh nếu cần
    List<Dish> findByRestaurant_Id(Long restaurantId);
    List<Dish> findByDishCategory_IdAndStatus(Long categoryId, boolean status);
    List<Dish> findByRestaurant_IdAndStatusAndNameContaining(Long accountId, boolean status, Pageable pageable, String query);
    int countByRestaurant_IdAndStatusAndNameContaining(Long accountId, boolean status, String query);
    boolean existsByUnit_Id(Long unitId);
    List<Dish> findByRestaurant_IdAndStatus(Long restaurantId, boolean status);
}
