package com.restaurent.manager.dto.response.order;

import com.restaurent.manager.dto.response.CustomerResponse;
import com.restaurent.manager.dto.response.TableRestaurantResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    LocalDate orderDate;
    TableRestaurantResponse tableRestaurant;
    CustomerResponse customer;
    double totalMoney;
    int totalDish;
}
