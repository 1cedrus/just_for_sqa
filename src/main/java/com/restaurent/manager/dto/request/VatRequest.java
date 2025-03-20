package com.restaurent.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VatRequest {
    @NotNull(message = "NOT_EMPTY")
    @NotBlank(message = "NOT_EMPTY")
    String name;
    @NotNull(message = "INVALID_VAT_CODE")
    @NotBlank(message = "INVALID_VAT_CODE")
    String taxCode;
    @NotNull(message = "INVALID_USERNAME")
    @NotBlank(message = "INVALID_USERNAME")
    String address;
    String branch;
    String registrationNumber;
}
