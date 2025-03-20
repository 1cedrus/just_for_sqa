package com.restaurent.manager.dto.response.order;

import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.dto.response.DishResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DishOrderResponse {
    Long id;
    DishResponse dish;
    ComboResponse combo;
    OrderResponse order;
    int quantity;
    String status;
    LocalDateTime orderDate;
}
