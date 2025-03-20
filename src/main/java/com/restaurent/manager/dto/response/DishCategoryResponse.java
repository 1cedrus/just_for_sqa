package com.restaurent.manager.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DishCategoryResponse {
    Long id;
    String name;
    String code;
    String description;
}
