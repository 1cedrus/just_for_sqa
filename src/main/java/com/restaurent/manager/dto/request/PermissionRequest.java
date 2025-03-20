package com.restaurent.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String name;
    @NotNull(message = "NOT_NULL")
    @NotBlank(message = "NOT_EMPTY")
    private String description;
}
