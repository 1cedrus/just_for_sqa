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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void setTableRestaurants(Set<TableRestaurant> tableRestaurants) {
        this.tableRestaurants = tableRestaurants;
    }

    public Set<TableRestaurant> getTableRestaurants() {
        return tableRestaurants;
    }
}
