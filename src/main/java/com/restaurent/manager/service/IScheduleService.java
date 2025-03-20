package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.ScheduleRequest;
import com.restaurent.manager.dto.response.ScheduleResponse;
import com.restaurent.manager.dto.response.ScheduleTimeResponse;
import com.restaurent.manager.entity.Schedule;
import com.restaurent.manager.enums.SCHEDULE_STATUS;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleService {
    String createSchedule(Long restaurantId,ScheduleRequest request);
    boolean checkTableIsBooked(Long tableId,ScheduleRequest request);
    List<ScheduleResponse> findScheduleRestaurantByDate(Long restaurantId,LocalDate date);
    List<ScheduleResponse> findScheduleRestaurantLate(Long restaurantId);
    List<ScheduleResponse> findScheduleRestaurantNearly(Long restaurantId);
    void updateStatusScheduleById(Long scheduleId, Long employeeId, SCHEDULE_STATUS status);
    List<ScheduleTimeResponse> getNumberScheduleRestaurantWithTime(Long restaurantId);
    List<ScheduleResponse> findAllScheduleRestaurant(Long restaurantId, Pageable pageable);
    void customerReceiveBookTable(Long employeeId ,Schedule schedule);
    String updateScheduleRestaurant(Long scheduleId, ScheduleRequest request);
    PagingResult<ScheduleResponse> findSchedulesByTableId(Long tableId, Pageable pageable);
    Schedule findById(Long scheduleId);
}
