package com.restaurent.manager.dto.request.employee;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeLoginRequest {
    @NotNull(message = "NOT_EMPTY")
    String phoneNumberOfRestaurant;
    @NotNull(message = "NOT_EMPTY")
    String username;
    @NotNull(message = "NOT_EMPTY")
    String password;
}
