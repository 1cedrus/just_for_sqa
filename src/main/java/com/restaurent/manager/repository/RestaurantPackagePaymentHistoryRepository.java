package com.restaurent.manager.repository;

import com.restaurent.manager.entity.RestaurantPackagePaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

public interface RestaurantPackagePaymentHistoryRepository extends JpaRepository<RestaurantPackagePaymentHistory,Long> {
    @Query("select r from RestaurantPackagePaymentHistory r  where  DATE(r.dateCreated) = :date")
    List<RestaurantPackagePaymentHistory> findByDateCreated(Date date);
}
