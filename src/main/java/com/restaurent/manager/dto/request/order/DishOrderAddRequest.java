package com.restaurent.manager.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishOrderAddRequest {
   @NotNull(message = "NOT_NULL")
   List<DishOrderRequest> dishOrderRequests;
   @NotNull(message = "NOT_NULL")
   Long orderId;
   @NotNull(message = "NOT_NULL")
   Long restaurantId;
}
