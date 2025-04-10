package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.response.ScheduleDishResponse;
import com.restaurent.manager.entity.Combo;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.Schedule;
import com.restaurent.manager.entity.ScheduleDish;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.ScheduleDishMapper;
import com.restaurent.manager.repository.ScheduleDishRepository;
import com.restaurent.manager.service.impl.ScheduleDishService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

// Sử dụng MockitoExtension để tích hợp Mockito với JUnit 5
@ExtendWith(MockitoExtension.class)
class ScheduleDishServiceTest {

    // Mô phỏng các phụ thuộc
    @Mock
    private IDishService dishService;

    @Mock
    private IComboService comboService;

    @Mock
    private ScheduleDishRepository repository;

    @Mock
    private ScheduleDishMapper scheduleDishMapper;

    // InjectMocks tạo instance ScheduleDishService và tiêm các mock vào
    @InjectMocks
    private ScheduleDishService service;

    @BeforeEach
    void setup() {
        // Đảm bảo trạng thái sạch sẽ trước mỗi test
    }

    // --- Kiểm thử cho createScheduleDish ---

    // Kiểm tra tạo với Dish (dishId không null)
    @Test
    void testCreateScheduleDish_WithDish() {
        // Chuẩn bị dữ liệu
        Schedule schedule = createSchedule(1L);
        DishOrderRequest request = createDishOrderRequest(1L, null, 2);
        Dish dish = createDish(1L);
        ScheduleDish scheduleDish = new ScheduleDish();

        when(dishService.findByDishId(1L)).thenReturn(dish);
        when(repository.save(any(ScheduleDish.class))).thenReturn(scheduleDish);

        // Thực thi
        service.createScheduleDish(schedule, request);

        // Kiểm tra
        verify(dishService, times(1)).findByDishId(1L);
        verify(comboService, never()).findComboById(anyLong());
        verify(repository, times(1)).save(any(ScheduleDish.class));
    }

    // Kiểm tra tạo với Combo (dishId null)
    @Test
    void testCreateScheduleDish_WithCombo() {
        // Chuẩn bị dữ liệu
        Schedule schedule = createSchedule(1L);
        DishOrderRequest request = createDishOrderRequest(null, 2L, 3);
        Combo combo = createCombo(2L);
        ScheduleDish scheduleDish = new ScheduleDish();

        when(comboService.findComboById(2L)).thenReturn(combo);
        when(repository.save(any(ScheduleDish.class))).thenReturn(scheduleDish);

        // Thực thi
        service.createScheduleDish(schedule, request);

        // Kiểm tra
        verify(dishService, never()).findByDishId(anyLong());
        verify(comboService, times(1)).findComboById(2L);
        verify(repository, times(1)).save(any(ScheduleDish.class));
    }

    // Kiểm tra tạo với Combo null và dish null
    @Test
    void testCreateScheduleDish_WithNullComboAndDish() {
        // Chuẩn bị dữ liệu
        Schedule schedule = createSchedule(1L);
        DishOrderRequest request = createDishOrderRequest(null, null, 3);

        // Thực thi
        AppException appException = assertThrows(AppException.class, () ->
                service.createScheduleDish(schedule, request)
        );
        // Kiểm tra
        assertEquals(ErrorCode.SCHEDULE_DISH_REQUEST_INVALID, appException.getErrorCode());
        verify(dishService, never()).findByDishId(anyLong());
        verify(comboService, never()).findComboById(anyLong());
        verify(repository, never()).save(any(ScheduleDish.class));
    }

    // --- Kiểm thử cho findDishOrComboBySchedule ---

    // Kiểm tra khi danh sách không rỗng
    @Test
    void testFindDishOrComboBySchedule_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Long scheduleId = 1L;
        ScheduleDish dish1 = createScheduleDish(1L);
        ScheduleDish dish2 = createScheduleDish(2L);
        List<ScheduleDish> dishes = Arrays.asList(dish1, dish2);
        ScheduleDishResponse response1 = createScheduleDishResponse(1L);
        ScheduleDishResponse response2 = createScheduleDishResponse(2L);

        when(repository.findBySchedule_Id(scheduleId)).thenReturn(dishes);
        when(scheduleDishMapper.toScheduleResponse(dish1)).thenReturn(response1);
        when(scheduleDishMapper.toScheduleResponse(dish2)).thenReturn(response2);

        // Thực thi
        List<ScheduleDishResponse> result = service.findDishOrComboBySchedule(scheduleId);

        // Kiểm tra
        verify(repository, times(1)).findBySchedule_Id(scheduleId);
        verify(scheduleDishMapper, times(1)).toScheduleResponse(dish1);
        verify(scheduleDishMapper, times(1)).toScheduleResponse(dish2);
        assertEquals(2, result.size());
    }

    // Kiểm tra khi danh sách rỗng
    @Test
    void testFindDishOrComboBySchedule_EmptyList() {
        // Chuẩn bị dữ liệu
        Long scheduleId = 1L;
        when(repository.findBySchedule_Id(scheduleId)).thenReturn(Collections.emptyList());

        // Thực thi
        List<ScheduleDishResponse> result = service.findDishOrComboBySchedule(scheduleId);

        // Kiểm tra
        verify(repository, times(1)).findBySchedule_Id(scheduleId);
        verify(scheduleDishMapper, never()).toScheduleResponse(any());
        assertTrue(result.isEmpty());
    }

    // --- Kiểm thử cho deleteScheduleDishById ---

    // Kiểm tra xóa ScheduleDish
    @Test
    void testDeleteScheduleDishById() {
        // Chuẩn bị dữ liệu
        Long scheduleDishId = 1L;

        // Thực thi
        service.deleteScheduleDishById(scheduleDishId);

        // Kiểm tra
        verify(repository, times(1)).deleteById(scheduleDishId);
    }

    // --- Kiểm thử cho findByScheduleId ---

    // Kiểm tra khi danh sách không rỗng
    @Test
    void testFindByScheduleId_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Long scheduleId = 1L;
        ScheduleDish dish1 = createScheduleDish(1L);
        ScheduleDish dish2 = createScheduleDish(2L);
        List<ScheduleDish> dishes = Arrays.asList(dish1, dish2);

        when(repository.findBySchedule_Id(scheduleId)).thenReturn(dishes);

        // Thực thi
        List<ScheduleDish> result = service.findByScheduleId(scheduleId);

        // Kiểm tra
        verify(repository, times(1)).findBySchedule_Id(scheduleId);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    // Kiểm tra khi danh sách rỗng
    @Test
    void testFindByScheduleId_EmptyList() {
        // Chuẩn bị dữ liệu
        Long scheduleId = 1L;
        when(repository.findBySchedule_Id(scheduleId)).thenReturn(Collections.emptyList());

        // Thực thi
        List<ScheduleDish> result = service.findByScheduleId(scheduleId);

        // Kiểm tra
        verify(repository, times(1)).findBySchedule_Id(scheduleId);
        assertTrue(result.isEmpty());
    }

    private Combo createCombo(Long id) {
        return Combo.builder()
                .id(id)
                .build();
    }

    private ScheduleDish createScheduleDish(Long id) {
        return ScheduleDish.builder()
                .id(id)
                .build();
    }

    private DishOrderRequest createDishOrderRequest(
            Long dishId,
            Long comboId,
            int quantity
    ) {
        return DishOrderRequest.builder()
                .dishId(dishId)
                .comboId(comboId)
                .quantity(quantity)
                .build();
    }

    private Dish createDish(Long dishId) {
        return Dish.builder()
                .id(dishId)
                .build();
    }

    private Schedule createSchedule(Long scheduleId) {
        return Schedule.builder()
                .id(scheduleId)
                .build();
    }

    private ScheduleDishResponse createScheduleDishResponse(Long id) {
        return ScheduleDishResponse.builder()
                .id(id)
                .build();
    }
}