package com.restaurent.manager.dto.request.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsRequest {
    private double moneyToPoint;
    private double pointToMoney;
}
