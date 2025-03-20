package com.restaurent.manager.dto.request.dish;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishRequest {
    @NotNull(message = "NAME_EMPTY")
    @NotBlank(message = "NAME_EMPTY")
    String name;
    @NotNull(message = "DESCRIPTION_NOT_NULL")
    @NotBlank(message = "DESCRIPTION_NOT_NULL")
    String description;
    @NotNull(message = "NOT_NULL")
    @Min(value = 1, message = "GREATER_NUMBER")
    double price;
    @NotNull(message = "CATEGORY_NOT_NULL")
    Long dishCategoryId;
    @NotNull(message = "IMAGE_NOT_NULL")
    @NotBlank(message = "IMAGE_NOT_NULL")
    String imageUrl;
    @NotNull(message = "UNIT_NOT_NULL")
    Long unitId;
    @NotNull(message = "RESTAURANT_NOT_EXISTED")
    Long restaurantId;
}
