package com.restaurent.manager.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class StatisticTableResponse {
    LocalDate time;
    int numbersBill;
    double profit;
}
