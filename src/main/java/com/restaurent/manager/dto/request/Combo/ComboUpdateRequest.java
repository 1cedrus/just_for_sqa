package com.restaurent.manager.dto.request.Combo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboUpdateRequest {


    @Valid
    @NotNull(message = "INVALID_COMBO_NAME")
    @NotBlank(message = "INVALID_COMBO_NAME")
    String name;

    @NotNull(message = "INVALID_COMBO_PRICE")
    @Positive(message = "COMBO_PRICE_MUST_BE_POSITIVE")
    double price;

    @NotNull(message = "INVALID_COMBO_DESCRIPTION")
    @NotBlank(message = "INVALID_COMBO_DESCRIPTION")
    String description;

    boolean status;
    Set<Long> dishIds;
}
