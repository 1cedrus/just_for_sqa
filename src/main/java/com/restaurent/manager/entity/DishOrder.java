package com.restaurent.manager.entity;

import com.restaurent.manager.enums.DISH_ORDER_STATE;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    Dish dish;
    @ManyToOne(fetch = FetchType.EAGER)
    Order order;
    @ManyToOne(fetch = FetchType.EAGER)
    Combo combo;
    int quantity;
    @Enumerated(EnumType.STRING)
    DISH_ORDER_STATE status;
    LocalDateTime orderDate;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DishOrder )) return false;
        return id != null && id.equals(((DishOrder) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
