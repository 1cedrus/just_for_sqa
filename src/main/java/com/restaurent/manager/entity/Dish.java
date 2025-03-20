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

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDishCategory(DishCategory dishCategory) {
        this.dishCategory = dishCategory;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setDishOrders(Set<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public float getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public DishCategory getDishCategory() {
        return dishCategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isStatus() {
        return status;
    }

    public Unit getUnit() {
        return unit;
    }

    public Set<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public Account getAccount() {
        return account;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
}
