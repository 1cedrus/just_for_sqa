package com.restaurent.manager.dto.response;

import com.restaurent.manager.enums.SCHEDULE_STATUS;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleResponse {
    Long id;
    String customerName;
    String customerPhone;
    String note;
    LocalDate bookedDate;
    LocalTime time;
    double deposit;
    LocalTime intendTime;
    int numbersOfCustomer;
    List<TableRestaurantResponse> tableRestaurants;
    SCHEDULE_STATUS status;
    List<ScheduleDishResponse> dishes;
}
