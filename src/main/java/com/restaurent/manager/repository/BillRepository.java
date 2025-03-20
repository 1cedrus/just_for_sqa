package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Bill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill,Long> {
    List<Bill> findByOrder_Restaurant_Id(Long restaurantId, Pageable pageable);
    @Query("select b from Order o join o.bill b where o.restaurant.id = :restaurantId and DATE(b.dateCreated) = :date")
    List<Bill> findByDateCreated(@Param("restaurantId") Long restaurantId,@Param("date") Date date);
    @Query("select b from Order o join o.bill b where o.restaurant.id = :restaurantId and b.dateCreated between :startDate and :endDate")
    List<Bill> findByDateCreatedBetween(@Param("restaurantId") Long restaurantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    @Query("SELECT b FROM Bill b join b.order o WHERE o.restaurant.id = :restaurantId AND DATE(b.dateCreated) = CURRENT_DATE AND TIME(b.dateCreated) BETWEEN :startTime AND :endTime")
    List<Bill> findByTimeBetweenAndCurrentDate(@Param("restaurantId") Long restaurantId,@Param("startTime") String startTime, @Param("endTime") String endTime);
    @Query("SELECT e FROM Bill e join e.order o WHERE o.restaurant.id = :restaurantId AND YEARWEEK(e.dateCreated, 1) = YEARWEEK(CURRENT_DATE, 1) AND TIME(e.dateCreated) BETWEEN :startTime AND :endTime")
    List<Bill> findByTimeBetweenAndCurrentWeek(@Param("restaurantId") Long restaurantId, @Param("startTime") String startTime, @Param("endTime") String endTime);
    @Query("SELECT e FROM Bill e join e.order o WHERE o.restaurant.id = :restaurantId AND YEAR(e.dateCreated) = YEAR(CURRENT_DATE) AND MONTH(e.dateCreated) = MONTH(CURRENT_DATE) AND TIME(e.dateCreated) BETWEEN :startTime AND :endTime")
    List<Bill> findByTimeBetweenAndCurrentMonth(@Param("restaurantId") Long restaurantId, @Param("startTime") String startTime, @Param("endTime") String endTime);

}
