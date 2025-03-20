package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String code;
    float weight;
    String description;
    double price;
    @ManyToOne(fetch = FetchType.EAGER)
    DishCategory dishCategory;
    String imageUrl;
    boolean status;
    @ManyToOne(fetch = FetchType.EAGER)
    Unit unit;
    @OneToMany(mappedBy = "dish")
    Set<DishOrder> dishOrders;
    @ManyToOne(fetch = FetchType.LAZY)
    Account account;
    @ManyToOne(fetch = FetchType.LAZY)
    Restaurant restaurant;
}
