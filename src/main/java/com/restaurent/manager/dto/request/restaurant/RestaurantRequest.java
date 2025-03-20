package com.restaurent.manager.dto.request.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantRequest {
    @Valid
    private String restaurantName;
    @NotNull(message = "id account is necessary for create Restaurant")
    private Long accountId;
    private String address;
    private String province;
    private String district;
}
