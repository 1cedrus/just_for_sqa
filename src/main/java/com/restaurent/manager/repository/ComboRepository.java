package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Combo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {
    @Query("SELECT c FROM Combo c WHERE c.status = true")
    List<Combo> findAllActiveCombos();
    List<Combo> findByRestaurant_IdAndNameContaining(Long restaurantId, String query,Pageable pageable);
    int countByRestaurant_IdAndNameContaining(Long restaurantId, String query);
}