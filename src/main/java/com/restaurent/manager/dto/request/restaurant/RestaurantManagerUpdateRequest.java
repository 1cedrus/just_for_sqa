package com.restaurent.manager.dto.request.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantManagerUpdateRequest {
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String restaurantName;
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String address;
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String province;
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String district;
}
