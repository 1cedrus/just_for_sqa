package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhoneNumberAndRestaurantId(String phoneNumber, Long restaurantId);
    List<Customer> findByRestaurant_IdAndNameContainingOrderByTotalPointDesc(Long restaurantId, String query, Pageable pageable);
    List<Customer> findByRestaurant_IdAndPhoneNumberContainingOrderByTotalPointDesc(Long restaurantId, String query, Pageable pageable);
    int countByRestaurant_IdAndNameContaining(Long restaurantId,String query);
    Optional<Customer> findByPhoneNumberAndRestaurant_Id(String phoneNumber, Long restaurantId);
    @Query("select c from Customer  c where c.restaurant.id = :restaurantId and DATE (c.dateCreated) = CURRENT_DATE")
    List<Customer> findCustomerByRestaurant_IdInToday(Long restaurantId);
    @Query("select c from Customer c where c.restaurant.id = :restaurantId and DATE (c.dateCreated) = :date")
    List<Customer> findCustomerByRestaurant_IdInYesterday(Long restaurantId, Date date);
    @Query("select c from Customer c where c.restaurant.id = :restaurantId and c.dateCreated between :startDate and :endDate")
    List<Customer> findCustomerByRestaurantIdInStartDateAndEndDate(@Param("restaurantId") Long restaurantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    boolean existsByPhoneNumberAndRestaurant_Id(String phoneNumber, Long restaurantId);
}
