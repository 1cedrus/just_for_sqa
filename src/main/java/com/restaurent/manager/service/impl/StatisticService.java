package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.response.StatisticTableResponse;
import com.restaurent.manager.dto.response.StatisticChartValueManager;
import com.restaurent.manager.dto.response.StatisticResponse;
import com.restaurent.manager.repository.BillRepository;
import com.restaurent.manager.repository.CustomerRepository;
import com.restaurent.manager.service.IBillService;
import com.restaurent.manager.service.IStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class StatisticService  implements IStatisticService{
    CustomerRepository customerRepository;
    BillRepository billRepository;
    IBillService billService;
    @Override
    public StatisticResponse getStatisticRestaurantById(Long restaurantId,String day) {
        return switch (day) {
            case "1" -> StatisticResponse.builder()
                    .numbersCustomer(customerRepository.findCustomerByRestaurant_IdInToday(restaurantId).size())
                    .numbersBill(billRepository.findByDateCreated(restaurantId,Date.valueOf(LocalDateTime.now().toLocalDate())).size())
                    .profit(billService.getProfitRestaurantByIdAndDate(restaurantId,LocalDateTime.now()))
                    .vat(billService.getVatValueForRestaurantCurrent(restaurantId, LocalDateTime.now()))
                    .build();
            case "-1" -> StatisticResponse.builder()
                    .numbersCustomer(customerRepository.findCustomerByRestaurant_IdInYesterday(restaurantId, Date.valueOf(LocalDateTime.now().minusDays(1).toLocalDate())).size())
                    .numbersBill(billRepository.findByDateCreated(restaurantId,Date.valueOf(LocalDateTime.now().minusDays(1).toLocalDate())).size())
                    .profit(billService.getProfitRestaurantByIdAndDate(restaurantId,LocalDateTime.now().minusDays(1)))
                    .vat(billService.getVatValueForRestaurantCurrent(restaurantId, LocalDateTime.now().minusDays(1)))
                    .build();
            default -> null;
        };
    }

    @Override
    public StatisticResponse getStatisticByRestaurantIdBetweenStartDayToEndDay(Long restaurantId, LocalDateTime start, LocalDateTime end) {
        return StatisticResponse.builder()
                .numbersCustomer(customerRepository.findCustomerByRestaurantIdInStartDateAndEndDate(restaurantId, start, end).size())
                .numbersBill(billRepository.findByDateCreatedBetween(restaurantId, start, end).size())
                .profit(billService.getProfitRestaurantByIdAndDateBetween(restaurantId, start, end))
                .vat(billService.getVatValueForRestaurantBetween(restaurantId, start, end))
                .build();
    }

    @Override
    public List<StatisticTableResponse> getDetailStatisticRestaurantEachOfDayInCurrentMonth(Long restaurantId) {
        List<StatisticTableResponse> responses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int currentDay = now.getDayOfMonth();
        for (int i = 0; i < currentDay; i++) {
            LocalDate localDate = now.with(TemporalAdjusters.firstDayOfMonth()).plusDays(i).toLocalDate();
            responses.add(StatisticTableResponse.builder()
                            .time(localDate)
                            .numbersBill(billRepository.findByDateCreated(restaurantId,Date.valueOf(localDate)).size())
                            .profit(billService.getProfitRestaurantByIdAndDate(restaurantId,now.with(TemporalAdjusters.firstDayOfMonth()).plusDays(i)))
                    .build());
        }
        return responses;
    }

    @Override
    public List<StatisticTableResponse> getDetailStatisticRestaurantEachOfDayInLastMonth(Long restaurantId) {
        List<StatisticTableResponse> responses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().minusMonths(1);
        int currentDay = now.getDayOfMonth();
        for (int i = 0; i < currentDay; i++) {
            responses.add(StatisticTableResponse.builder()
                    .time(now.with(TemporalAdjusters.firstDayOfMonth()).plusDays(i).toLocalDate())
                    .profit(billService.getProfitRestaurantByIdAndDate(restaurantId,now.with(TemporalAdjusters.firstDayOfMonth()).plusDays(i)))
                    .build());
        }
        return responses;
    }
    @Override
    public List<StatisticChartValueManager> getValueByTimeAndCurrentDateForRestaurant(Long restaurantId) {
        List<StatisticChartValueManager> res = new ArrayList<>();
        String[] timeArr = {"00:00","01:00","02:00","03:00","04:00","05:00","06:00","07:00","08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00"};
        for (int i = 0; i < timeArr.length; i++) {
            if(i == 0){
                res.add(StatisticChartValueManager.builder()
                        .time(timeArr[i])
                        .value(billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId,timeArr[timeArr.length - 1],timeArr[i]))
                        .build());
            }else{
                res.add(StatisticChartValueManager.builder()
                        .time(timeArr[i])
                        .value(billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId,timeArr[i - 1],timeArr[i]))
                        .build());
            }
        }
        return res;
    }

    @Override
    public List<StatisticTableResponse> getDetailStatisticRestaurantEachOfDayInCurrentWeek(Long restaurantId) {
        List<StatisticTableResponse> responses = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate currentDate = startOfWeek;
       while(!currentDate.isAfter(endOfWeek)){
           responses.add(StatisticTableResponse.builder()
                   .time(currentDate)
                   .profit(billService.getProfitRestaurantByIdAndDate(restaurantId, currentDate.atStartOfDay()))
                           .numbersBill(billRepository.findByDateCreated(restaurantId,Date.valueOf(currentDate)).size())
                   .build());
           currentDate = currentDate.plusDays(1);
       }
        return responses;
    }

    @Override
    public List<StatisticTableResponse> getDetailStatisticRestaurantEachOfDayInLastWeek(Long restaurantId) {
        List<StatisticTableResponse> responses = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).minusWeeks(1);
        LocalDate currentDate = startOfWeek;
        while(!currentDate.isAfter(endOfWeek)){
            responses.add(StatisticTableResponse.builder()
                    .time(currentDate)
                    .profit(billService.getProfitRestaurantByIdAndDate(restaurantId, currentDate.atStartOfDay()))
                    .numbersBill(billRepository.findByDateCreated(restaurantId,Date.valueOf(currentDate)).size())
                    .build());
            currentDate = currentDate.plusDays(1);
        }
        return responses;
    }
}
