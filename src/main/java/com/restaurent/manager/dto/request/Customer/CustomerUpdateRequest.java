package com.restaurent.manager.dto.request.Customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest {
    Long id;
    @Valid
    @NotNull(message = "INVALID_PHONENUMBER")
    @NotBlank(message = "INVALID_PHONENUMBER")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "INVALID_PHONENUMBER")
    String phoneNumber;

    @NotNull(message = "INVALID_CUSTOMER_NAME")
    @NotBlank(message = "INVALID_CUSTOMER_NAME")
    String name;

    String address;
    Long restaurantId;
}
