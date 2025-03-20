package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
List<Restaurant> findByRestaurantPackageIdIsNotNullAndExpiryDateBefore(LocalDateTime time);
Restaurant findByAccount_Id(Long accountID);
boolean existsByRestaurantName(String restaurantName);
boolean existsByAccount_Id(Long accountId);
int countByRestaurantPackage_Id(Long id);
int countByDateCreated(LocalDate date);
}
