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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrderCurrent() {
        return orderCurrent;
    }

    public void setOrderCurrent(Long orderCurrent) {
        this.orderCurrent = orderCurrent;
    }

    public int getNumberChairs() {
        return numberChairs;
    }

    public void setNumberChairs(int numberChairs) {
        this.numberChairs = numberChairs;
    }

    public TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
