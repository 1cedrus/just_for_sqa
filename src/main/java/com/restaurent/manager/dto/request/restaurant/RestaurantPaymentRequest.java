package com.restaurent.manager.dto.request.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantPaymentRequest {
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String BANK_ID;
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String ACCOUNT_NO;
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String ACCOUNT_NAME;
}
