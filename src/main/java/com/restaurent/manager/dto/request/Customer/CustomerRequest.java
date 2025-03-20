package com.restaurent.manager.dto.request.Customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class CustomerRequest {

     @Valid
     @NotNull(message = "INVALID_PHONENUMBER")
     @NotBlank(message = "INVALID_PHONENUMBER")
     @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "INVALID_PHONENUMBER")
     String phoneNumber;

     @NotNull(message = "INVALID_CUSTOMER_NAME")
     @NotBlank(message = "INVALID_CUSTOMER_NAME")
     @Size(max = 30, message = "CUSTOMER_NAME_INVALID")
     String name;
     @Size(max = 100, message = "ADDRESS_INVALID")
     String address;
     @NotNull(message = "NOT_NULL")
     Long restaurantId;
}
