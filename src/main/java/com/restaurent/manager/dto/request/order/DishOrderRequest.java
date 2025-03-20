package com.restaurent.manager.dto.request.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DishOrderRequest {
    @NotBlank(message = "NOT_NULL")
    Long dishId;
    @NotNull(message = "NOT_NULL")
    Long comboId;
    @NotNull(message = "NOT_NULL")
    @Min(value = 1, message = "QUANTITY_INVALID")
    int quantity;
}
