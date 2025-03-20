package com.restaurent.manager.controller;

import com.restaurent.manager.dto.response.StatisticTableResponse;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.StatisticAdminTable;
import com.restaurent.manager.dto.response.StatisticChartValueManager;
import com.restaurent.manager.dto.response.StatisticResponse;
import com.restaurent.manager.service.IRestaurantPackagePaymentHistoryService;
import com.restaurent.manager.service.IStatisticService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/statistic")
@FieldDefaults(makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
public class StatisticController {
    IStatisticService statisticService;
    IRestaurantPackagePaymentHistoryService service;
    @GetMapping(value = "/manager/restaurant/{restaurantId}")
    public ApiResponse<StatisticResponse> getStatisticRestaurantByIdInDay(@PathVariable Long restaurantId, @RequestParam(name = "day",defaultValue = "1") String day){
        return ApiResponse.<StatisticResponse>builder()
                .result(statisticService.getStatisticRestaurantById(restaurantId,day))
                .build();
    }
    @GetMapping(value = "/manager/restaurant/{restaurantId}/current-month")
    public ApiResponse<StatisticResponse> getStatisticRestaurantByIdInCurrentMonth(@PathVariable Long restaurantId){
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startDate = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endDate = today.with(TemporalAdjusters.lastDayOfMonth());
        return ApiResponse.<StatisticResponse>builder()
                .result(statisticService.getStatisticByRestaurantIdBetweenStartDayToEndDay(restaurantId,startDate,endDate))
                .build();
    }
    @GetMapping(value = "/manager/restaurant/{restaurantId}/last-month")
    public ApiResponse<StatisticResponse> getStatisticRestaurantByIdInLastMonth(@PathVariable Long restaurantId){
        LocalDateTime today = LocalDateTime.now().minusMonths(1);
        LocalDateTime startDate = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endDate = today.with(TemporalAdjusters.lastDayOfMonth());
        return ApiResponse.<StatisticResponse>builder()
                .result(statisticService.getStatisticByRestaurantIdBetweenStartDayToEndDay(restaurantId,startDate,endDate))
                .build();
    }
    @GetMapping(value = "/manager/restaurant/{restaurantId}/last-week/table")
    public ApiResponse<List<StatisticTableResponse>> getStatisticRestaurantByIdInLastWeek(@PathVariable Long restaurantId){
        return ApiResponse.<List<StatisticTableResponse>>builder()
                .result(statisticService.getDetailStatisticRestaurantEachOfDayInLastWeek(restaurantId))
                .build();
    }
    @GetMapping(value = "/manager/restaurant/{restaurantId}/current-week/table")
    public ApiResponse<List<StatisticTableResponse>> getStatisticRestaurantByIdInCurrentWeek(@PathVariable Long restaurantId){
        return ApiResponse.<List<StatisticTableResponse>>builder()
                .result(statisticService.getDetailStatisticRestaurantEachOfDayInCurrentWeek(restaurantId))
                .build();
    }
    @GetMapping(value = "/manager/restaurant/{restaurantId}/current-month/table")
    public ApiResponse<List<StatisticTableResponse>> getTableStatisticRestaurantByIdInCurrentMonth(@PathVariable Long restaurantId){
        return ApiResponse.<List<StatisticTableResponse>>builder()
                .result(statisticService.getDetailStatisticRestaurantEachOfDayInCurrentMonth(restaurantId))
                .build();
    }
    @GetMapping(value = "/manager/restaurant/{restaurantId}/last-month/table")
    public ApiResponse<List<StatisticTableResponse>> getTableStatisticRestaurantByIdInLastMonth(@PathVariable Long restaurantId){
        return ApiResponse.<List<StatisticTableResponse>>builder()
                .result(statisticService.getDetailStatisticRestaurantEachOfDayInLastMonth(restaurantId))
                .build();
    }
    @GetMapping(value = "/manager/restaurant/{restaurantId}/time")
    public ApiResponse<List<StatisticChartValueManager>> getTotalValueByTimeForRestaurant(@PathVariable Long restaurantId){
        return ApiResponse.<List<StatisticChartValueManager>>builder()
                .result(statisticService.getValueByTimeAndCurrentDateForRestaurant(restaurantId))
                .build();
    }
    @GetMapping(value = "/admin/time/{code}")
    public ApiResponse<List<StatisticAdminTable>> getTotalValueByDate(@PathVariable String code){
        return ApiResponse.<List<StatisticAdminTable>>builder()
                .result(service.getTotalValueByDate(code))
                .build();
    }
}
