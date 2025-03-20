package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.response.ScheduleDishResponse;
import com.restaurent.manager.entity.Schedule;
import com.restaurent.manager.entity.ScheduleDish;
import com.restaurent.manager.mapper.ScheduleDishMapper;
import com.restaurent.manager.repository.ScheduleDishRepository;
import com.restaurent.manager.service.IComboService;
import com.restaurent.manager.service.IDishService;
import com.restaurent.manager.service.IScheduleDishService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
public class ScheduleDishService implements IScheduleDishService {

    IDishService dishService;
    IComboService comboService;
    ScheduleDishRepository repository;
    ScheduleDishMapper scheduleDishMapper;
    @Override
    public void createScheduleDish(Schedule schedule, DishOrderRequest request) {
        ScheduleDish scheduleDish = new ScheduleDish();
        if(request.getDishId() != null){
            scheduleDish.setDish(dishService.findByDishId(request.getDishId()));
        }else{
            scheduleDish.setCombo(comboService.findComboById(request.getComboId()));
        }
        scheduleDish.setSchedule(schedule);
        scheduleDish.setQuantity(request.getQuantity());
        repository.save(scheduleDish);
    }

    @Override
    public List<ScheduleDishResponse> findDishOrComboBySchedule(Long scheduleId) {
        return repository.findBySchedule_Id(scheduleId).stream().map(scheduleDishMapper::toScheduleResponse).toList();
    }

    @Override
    public void deleteScheduleDishById(Long scheduleDishId) {
        repository.deleteById(scheduleDishId);
    }

    @Override
    public List<ScheduleDish> findByScheduleId(Long scheduleId) {
        return repository.findBySchedule_Id(scheduleId);
    }
}
