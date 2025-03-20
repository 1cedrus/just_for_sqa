package com.restaurent.manager.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishResponse {
    Long id;
    String name;
    String code;
    boolean status;
    String description;
    double price;
    DishCategoryResponse dishCategory;
    String imageUrl;
    UnitResponse unit;
}
