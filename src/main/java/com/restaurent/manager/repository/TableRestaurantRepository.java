package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Order;
import com.restaurent.manager.entity.TableRestaurant;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TableRestaurantRepository extends JpaRepository<TableRestaurant,Long> {
    List<TableRestaurant> findByArea_IdAndHidden(Long areaId,boolean hidden);
    boolean existsByNameAndArea_Id(String name,Long areaId);
    @Query("select tb from TableRestaurant tb join Area a on a.id = tb.area.id where tb.name like :name% and a.restaurant.id = :restaurantId ORDER BY tb.id DESC LIMIT 1")
    TableRestaurant findTopByRestaurant_IdAndNameStartingWithOrderByNameDesc(Long restaurantId,String name);

}
