package com.restaurent.manager.dto.request.Table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableRestaurantUpdateRequest {
    Long id;
    @NotNull(message = "NAME_NULL")
    @NotEmpty(message = "NAME_NULL")
    String name;
    @Min(value = 4,message = "NUMBER_CHAIRS_TOO_FEW")
    int numberChairs;
    @NotNull(message = "TABLE_TYPE_NULL")
    Long tableTypeId;
    @NotNull(message = "AREA_NULL")
    Long areaId;
    float positionX;
    float positionY;
}
