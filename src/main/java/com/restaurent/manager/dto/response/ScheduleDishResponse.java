package com.restaurent.manager.dto.response;

import com.restaurent.manager.dto.response.Combo.ComboResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ScheduleDishResponse {
    Long id;
    ComboResponse combo;
    DishResponse dish;
    int quantity;
}
