package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String phoneNumber;
    String name;
    String address;
    float currentPoint;
    float totalPoint;
    @ManyToOne(fetch = FetchType.LAZY)
    Restaurant restaurant;
    @OneToMany(mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    Set<Order> orders;
    LocalDateTime dateCreated;

    public void setId(Long id) {
        this.id = id;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCurrentPoint(float currentPoint) {
        this.currentPoint = currentPoint;
    }

    public void setTotalPoint(float totalPoint) {
        this.totalPoint = totalPoint;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public float getCurrentPoint() {
        return currentPoint;
    }

    public float getTotalPoint() {
        return totalPoint;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
}
