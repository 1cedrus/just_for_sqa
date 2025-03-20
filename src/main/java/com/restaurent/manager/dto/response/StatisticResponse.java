package com.restaurent.manager.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticResponse {
    int numbersCustomer;
    int numbersBill;
    double profit;
    double vat;
}
