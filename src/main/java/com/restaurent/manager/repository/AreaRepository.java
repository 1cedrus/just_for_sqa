package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area,Long> {
    List<Area> findByRestaurant_Id(Long restaurantId);
    int countByRestaurant_Id(Long restaurantId);
}
