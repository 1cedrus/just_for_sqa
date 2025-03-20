package com.restaurent.manager.dto.response;

import com.restaurent.manager.dto.response.Pack.PackageResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RestaurantPackageHistoryResponse {
    Long id;
    PackageResponse pack;
    RestaurantResponse restaurant;
    double totalMoney;
    int months;
    LocalDateTime dateCreated;
}
