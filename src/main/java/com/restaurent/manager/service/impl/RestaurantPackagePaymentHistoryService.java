package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.RestaurantPackagePaymentHistoryRequest;
import com.restaurent.manager.dto.request.restaurant.RestaurantUpdateRequest;
import com.restaurent.manager.dto.response.StatisticAdminTable;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.entity.RestaurantPackagePaymentHistory;
import com.restaurent.manager.enums.EmailContainer;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.RestaurantPackageHistoryMapper;
import com.restaurent.manager.repository.RestaurantPackagePaymentHistoryRepository;
import com.restaurent.manager.service.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
public class RestaurantPackagePaymentHistoryService implements IRestaurantPackagePaymentHistoryService {
    RestaurantPackagePaymentHistoryRepository restaurantPackagePaymentHistoryRepository;
    IPackageService packageService;
    IRestaurantService restaurantService;
    RestaurantPackageHistoryMapper mapper;
    AccountService accountService;
    IEmailService emailService;
    @Override
    public Long createRestaurantPackagePaymentHistory(RestaurantPackagePaymentHistoryRequest request) {
        RestaurantPackagePaymentHistory restaurantPackagePaymentHistory = mapper.toRestaurantPackagePaymentHistory(request);
        packageService.findPackById(request.getPackageId());
        restaurantService.getRestaurantById(request.getRestaurantId());
        restaurantPackagePaymentHistory.setId(getNewId());
        restaurantPackagePaymentHistory.setDateCreated(LocalDateTime.now());
        return restaurantPackagePaymentHistoryRepository.save(restaurantPackagePaymentHistory).getId();
    }

    @Override
    public Long getNewId() {
        List<RestaurantPackagePaymentHistory> all = restaurantPackagePaymentHistoryRepository.findAll();
        return all.isEmpty() ? 1L : all.getLast().getId() + 1;
    }

    @Override
    public String updateRestaurantPackagePaymentHistory(Long id, RestaurantPackagePaymentHistoryRequest request) {
        RestaurantPackagePaymentHistory history = restaurantPackagePaymentHistoryRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
        restaurantService.updateRestaurant(request.getRestaurantId(), RestaurantUpdateRequest.builder()
                .months(request.getMonths())
                .packId(request.getPackageId())
                .build());
        history.setPaid(true);
        Package pack = packageService.findPackById(request.getPackageId());
        Account account = accountService.findAccountByID(request.getAccountId());
        String token = accountService.generateToken(account);
        restaurantPackagePaymentHistoryRepository.save(history);
        emailService.sendEmail(account.getEmail(), EmailContainer.formMailPackage(pack.getPackName()),"Updated Package for Restaurant");
        return token;
    }

    @Override
    public List<StatisticAdminTable> getTotalValueByDate(String code) {
        return switch (code) {
            case "current-week" -> getProfitInCurrentWeek();
            case "current-month" -> getProfitInCurrentMonth();
            case "last-week" -> getProfitInLastWeek();
            case "last-month" -> getProfitInLastMonth();
            default -> null;
        };
    }

    @Override
    public List<StatisticAdminTable> getProfitInCurrentMonth() {
        List<StatisticAdminTable> res = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int currentDay = now.getDayOfMonth();
        for (int i = 0; i < currentDay; i++){
            LocalDate localDate = now.with(TemporalAdjusters.firstDayOfMonth()).plusDays(i).toLocalDate();
            res.add(StatisticAdminTable.builder()
                            .day(localDate)
                            .totalRestaurant(restaurantService.countRestaurantByDateCreated(localDate))
                            .total(totalValueInDate(localDate))
                    .build());
        }
        return res;
    }

    @Override
    public List<StatisticAdminTable> getProfitInLastMonth() {
        List<StatisticAdminTable> res = new ArrayList<>();
        LocalDate now = LocalDate.now(); // Current date
        LocalDate firstDayOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()); // First day of last month
        LocalDate lastDayOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()); // Last day of last month

        LocalDate currentDate = firstDayOfLastMonth;
        while (!currentDate.isAfter(lastDayOfLastMonth)) {
            res.add(StatisticAdminTable.builder()
                    .day(currentDate)
                    .total(totalValueInDate(currentDate))
                            .totalRestaurant(restaurantService.countRestaurantByDateCreated(currentDate))
                    .build());
            currentDate = currentDate.plusDays(1);
        }
        return res;
    }

    @Override
    public List<StatisticAdminTable> getProfitInCurrentWeek() {
        List<StatisticAdminTable> res = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate startOfWeek = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDate currentDate = startOfWeek;
        while (!currentDate.isAfter(endOfWeek)) {
            res.add(StatisticAdminTable.builder()
                    .day(currentDate)
                    .total(totalValueInDate(currentDate))
                    .totalRestaurant(restaurantService.countRestaurantByDateCreated(currentDate))
                    .build());
            currentDate = currentDate.plusDays(1);
        }
        return res;
    }

    @Override
    public List<StatisticAdminTable> getProfitInLastWeek() {
        List<StatisticAdminTable> res = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate startOfWeek = now.minusWeeks(1).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDate currentDate = startOfWeek;
        while (!currentDate.isAfter(endOfWeek)) {
            res.add(StatisticAdminTable.builder()
                    .day(currentDate)
                    .total(totalValueInDate(currentDate))
                    .totalRestaurant(restaurantService.countRestaurantByDateCreated(currentDate))
                    .build());
            currentDate = currentDate.plusDays(1);
        }
        return res;
    }

    @Override
    public double totalValueInDate(LocalDate date) {
        double res = 0;
        Date sqlDate = Date.valueOf(date);
        List<RestaurantPackagePaymentHistory> list = restaurantPackagePaymentHistoryRepository.findByDateCreated(sqlDate);
       if(!list.isEmpty()){
           for (RestaurantPackagePaymentHistory item : list){
               if(item.isPaid()){
                   res += item.getTotalMoney();
               }
           }
           return Math.round(res);
       }
        return 0;
    }
}
