package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.response.ScheduleDishResponse;
import com.restaurent.manager.entity.Schedule;
import com.restaurent.manager.entity.ScheduleDish;

import java.util.List;

public interface IScheduleDishService {
    void createScheduleDish(Schedule schedule, DishOrderRequest request);
    List<ScheduleDishResponse> findDishOrComboBySchedule(Long scheduleId);
    void deleteScheduleDishById(Long scheduleDishId);
    List<ScheduleDish> findByScheduleId(Long scheduleId);
}
