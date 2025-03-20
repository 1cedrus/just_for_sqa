package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.RestaurantPackagePaymentHistoryRequest;
import com.restaurent.manager.dto.response.StatisticAdminTable;

import java.time.LocalDate;
import java.util.List;

public interface IRestaurantPackagePaymentHistoryService {
Long createRestaurantPackagePaymentHistory(RestaurantPackagePaymentHistoryRequest request);
Long getNewId();
String updateRestaurantPackagePaymentHistory(Long id, RestaurantPackagePaymentHistoryRequest request);

List<StatisticAdminTable> getTotalValueByDate(String code);

List<StatisticAdminTable> getProfitInCurrentMonth();
List<StatisticAdminTable> getProfitInLastMonth();

List<StatisticAdminTable> getProfitInCurrentWeek();

List<StatisticAdminTable> getProfitInLastWeek();

double totalValueInDate(LocalDate date);
}
