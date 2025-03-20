package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;


@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableRestaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    Long orderCurrent;
    int numberChairs;
    @ManyToOne(fetch = FetchType.EAGER)
    TableType tableType;
    @OneToMany(mappedBy = "tableRestaurant",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    Set<Order> orders;
    @ManyToOne(fetch = FetchType.EAGER)
    Area area;
    float positionX;
    float positionY;
    boolean hidden;
}
