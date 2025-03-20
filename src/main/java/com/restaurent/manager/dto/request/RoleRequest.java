package com.restaurent.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleRequest {
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String name;
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String description;
}
