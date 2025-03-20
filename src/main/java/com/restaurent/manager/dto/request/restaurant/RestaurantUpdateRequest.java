package com.restaurent.manager.dto.request.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantUpdateRequest {
    @Valid
    @NotNull(message = "NOT_NULL")
    private Long packId;
    @NotNull(message = "NOT_NULL")
    @Min(value = 1, message = "MIN_VALUE")
    private int months;
}
