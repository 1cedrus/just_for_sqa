package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String code;
    String description;
    @OneToMany(mappedBy = "dishCategory",
    cascade = CascadeType.ALL
    )
    Set<Dish> dishes = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    Account account;
    @ManyToOne(fetch = FetchType.LAZY)
    Restaurant restaurant;
}
