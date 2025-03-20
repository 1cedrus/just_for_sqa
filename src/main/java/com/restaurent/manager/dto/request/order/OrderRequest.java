package com.restaurent.manager.dto.request.order;

import com.restaurent.manager.dto.response.CustomerResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    @NotNull(message = "NOT_NULL")
    Long tableId;
    @NotNull(message = "NOT_NULL")

    Long employeeId;
    @NotNull(message = "NOT_NULL")
    CustomerResponse customerResponse;
    @NotNull(message = "NOT_NULL")
    Long restaurantId;
}
