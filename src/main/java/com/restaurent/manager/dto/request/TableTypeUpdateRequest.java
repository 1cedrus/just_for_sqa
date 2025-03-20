package com.restaurent.manager.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableTypeUpdateRequest {
    @NotNull(message = "FIELD_EMPTY")
    Long id;
    @NotNull(message = "FIELD_EMPTY")
    String name;
}
