package com.restaurent.manager.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticAdminTable {
    LocalDate day;
    double total;
    int totalRestaurant;
}
