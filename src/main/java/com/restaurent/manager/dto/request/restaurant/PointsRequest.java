package com.restaurent.manager.dto.request.restaurant;

import lombok.Data;

@Data
public class PointsRequest {
    private double moneyToPoint;
    private double pointToMoney;
}
