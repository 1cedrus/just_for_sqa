package com.restaurent.manager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestaurantPackagePaymentHistory {
    @Id
    Long id;
    Long packageId;
    Long restaurantId;
    double totalMoney;
    int months;
    LocalDateTime dateCreated;
    boolean isPaid;
}
