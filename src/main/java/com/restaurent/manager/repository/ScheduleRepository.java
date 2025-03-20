package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Schedule;
import com.restaurent.manager.enums.SCHEDULE_STATUS;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> {
    @Query("SELECT s FROM Schedule s JOIN s.tableRestaurants tr " +
            "WHERE tr.id = :tableRestaurantId AND s.bookedDate = :bookedDate " +
            "AND s.intendTime BETWEEN :startTime AND :endTime " +
            "AND (s.status = 'PENDING')")
    List<Schedule> findSchedulesByTableAndDateRange(
            @Param("tableRestaurantId") Long tableRestaurantId,
            @Param("bookedDate") LocalDate bookedDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
    @Query("SELECT s FROM Schedule s JOIN s.tableRestaurants tr " +
            "WHERE tr.id = :tableRestaurantId AND s.bookedDate = :bookedDate " +
            "AND (s.status = 'PENDING' OR s.status = 'ACCEPT')")
    List<Schedule> findSchedulesByTableIdAndDate(
            @Param("tableRestaurantId") Long tableRestaurantId,
            @Param("bookedDate") LocalDate bookedDate,
            Pageable pageable
    );
    @Query("SELECT COUNT(s) FROM Schedule s JOIN s.tableRestaurants tr " +
            "WHERE tr.id = :tableRestaurantId AND s.bookedDate = :bookedDate " +
            "AND (s.status = 'PENDING' OR s.status = 'ACCEPT')")
    int countSchedulesByTableIdAndDate(
            @Param("tableRestaurantId") Long tableRestaurantId,
            @Param("bookedDate") LocalDate bookedDate
    );
    @Query("SELECT s FROM Schedule s JOIN s.tableRestaurants tr " +
            "WHERE tr.id = :tableRestaurantId AND s.bookedDate = :bookedDate " +
            "AND (s.status = 'PENDING')")
    List<Schedule> findByTableIdAndBookedDate(
            @Param("tableRestaurantId") Long tableRestaurantId,
            @Param("bookedDate") LocalDate bookedDate
    );
    List<Schedule> findByBookedDateAndRestaurant_IdAndStatus(LocalDate date, Long restaurantId,SCHEDULE_STATUS status);
    List<Schedule> findByRestaurant_IdAndBookedDateAndTimeIsBeforeAndStatus(Long restaurantID, LocalDate date, LocalTime time,SCHEDULE_STATUS status);
    List<Schedule> findByRestaurant_IdAndBookedDateAndTimeBetweenAndStatus(Long restaurantId, LocalDate date, LocalTime startTime, LocalTime endTime, SCHEDULE_STATUS status);
    int countByRestaurant_IdAndBookedDateAndStatus(Long restaurantId, LocalDate date,SCHEDULE_STATUS status);
    List<Schedule> findByRestaurant_IdAndStatus(Long restaurantId, Pageable pageable,SCHEDULE_STATUS status);
    Optional<Schedule> findByIdAndRestaurant_Id(Long scheduleId, Long restaurantId);
}
