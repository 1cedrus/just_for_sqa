package com.restaurent.manager.dto.response;

import com.restaurent.manager.dto.response.Pack.PackageResponse;
import com.restaurent.manager.entity.Vat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String restaurantName;
    private String address;
    private String province;
    private String district;
    private PackageResponse restaurantPackage;
    private LocalDateTime expiryDate;
    private String token;
    private String BANK_ID;
    private String ACCOUNT_NO;
    private String ACCOUNT_NAME;
    private Vat vat;
    private boolean vatActive;
    private double moneyToPoint;
    private double pointToMoney;
}
