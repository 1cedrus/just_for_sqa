package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    @ManyToOne(fetch = FetchType.LAZY)
    Restaurant restaurant;
    @OneToMany(mappedBy = "area",
        cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    Set<TableRestaurant> tableRestaurants;
}
