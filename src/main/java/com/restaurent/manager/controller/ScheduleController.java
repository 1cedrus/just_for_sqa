package com.restaurent.manager.controller;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.ScheduleRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.ScheduleResponse;
import com.restaurent.manager.dto.response.ScheduleTimeResponse;
import com.restaurent.manager.enums.SCHEDULE_STATUS;
import com.restaurent.manager.service.IScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/api/schedule")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {
    IScheduleService scheduleService;

    @PostMapping(value = "/restaurant/{restaurantId}/create")
    public ApiResponse<String> createSchedule(@PathVariable Long restaurantId, @RequestBody @Valid ScheduleRequest request){
        return ApiResponse.<String>builder()
                .result(scheduleService.createSchedule(restaurantId,request))
                .build();
    }
    @GetMapping(value = "/restaurant/{restaurantId}/time/{time}")
    public ApiResponse<List<ScheduleResponse>> findScheduleRestaurantByTime(@PathVariable Long restaurantId, @PathVariable String time){
        return switch (time) {
            case "late" -> ApiResponse.<List<ScheduleResponse>>builder()
                    .result(scheduleService.findScheduleRestaurantLate(restaurantId))
                    .build();
            case "near" -> ApiResponse.<List<ScheduleResponse>>builder()
                    .result(scheduleService.findScheduleRestaurantNearly(restaurantId))
                    .build();
            default -> ApiResponse.<List<ScheduleResponse>>builder()
                    .message("No schedule")
                    .build();
        };
    }
    @GetMapping(value = "/restaurant/{restaurantId}/date/{date}")
    public ApiResponse<List<ScheduleResponse>> findScheduleRestaurantByDate(@PathVariable Long restaurantId, @PathVariable LocalDate date){
        return ApiResponse.<List<ScheduleResponse>>builder()
                .result(scheduleService.findScheduleRestaurantByDate(restaurantId,date))
                .build();
    }
    @PutMapping(value = "/{scheduleId}/employee/{employeeId}/status/{status}")
    public ApiResponse<Void> updateScheduleRestaurant(@PathVariable Long scheduleId, @PathVariable Long employeeId ,@PathVariable SCHEDULE_STATUS status){
        scheduleService.updateStatusScheduleById(scheduleId,employeeId,status);
        return ApiResponse.<Void>builder().build();
    }
    @GetMapping(value = "/restaurant/{restaurantId}/day")
    public ApiResponse<List<ScheduleTimeResponse>> getScheduleRestaurantWithDay(@PathVariable Long restaurantId){
        return ApiResponse.<List<ScheduleTimeResponse>>builder()
                .result(scheduleService.getNumberScheduleRestaurantWithTime(restaurantId))
                .build();
    }
    @GetMapping(value = "/restaurant/{restaurantId}/find-all")
    public ApiResponse<List<ScheduleResponse>> findAllScheduleRestaurant(@PathVariable Long restaurantId,@RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "20") int size){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<List<ScheduleResponse>>builder()
                .result(scheduleService.findAllScheduleRestaurant(restaurantId,pageable))
                .build();
    }
    @PutMapping(value = "/{scheduleId}/update")
    public ApiResponse<String> updateScheduleById(@PathVariable Long scheduleId, @RequestBody @Valid ScheduleRequest request){
        return ApiResponse.<String>builder()
                .result(scheduleService.updateScheduleRestaurant(scheduleId,request))
                .build();
    }
    @GetMapping(value = "/table/{tableId}")
    public ApiResponse<PagingResult<ScheduleResponse>> findSchedulesByTableId(@PathVariable Long tableId, @RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<PagingResult<ScheduleResponse>>builder()
                .result(scheduleService.findSchedulesByTableId(tableId,pageable))
                .build();
    }
}
