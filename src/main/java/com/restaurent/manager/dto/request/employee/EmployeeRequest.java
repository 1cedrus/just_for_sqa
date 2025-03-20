package com.restaurent.manager.dto.request.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeRequest {
    @NotNull(message = "NOT_EMPTY")
    @NotBlank(message = "NOT_EMPTY")
    @Size(max = 15, message = "USERNAME_NAME_INVALID")
    String username;
    @NotNull(message = "NOT_EMPTY")
    @NotBlank(message = "NOT_EMPTY")
    @Size(min = 6,message = "INVALID_PASSWORD")
    String password;
    @NotNull(message = "NOT_EMPTY")
    @NotBlank(message = "NOT_EMPTY")
    String phoneNumber;
    @NotNull(message = "NOT_EMPTY")
    @NotBlank(message = "NOT_EMPTY")
    @Size(max = 30, message = "EMPLOYEE_NAME_INVALID")
    String employeeName;
    @NotNull(message = "NOT_EMPTY")
    Long accountId;
    @NotNull(message = "NOT_NULL")
    Long roleId;
}
