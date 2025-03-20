package com.restaurent.manager.entity;

import com.restaurent.manager.enums.DISH_ORDER_STATE;
import com.restaurent.manager.enums.SCHEDULE_STATUS;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String customerName;
    String customerPhone;
    String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getBookedDate() {
        return bookedDate;
    }

    public void setBookedDate(LocalDate bookedDate) {
        this.bookedDate = bookedDate;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public LocalTime getIntendTime() {
        return intendTime;
    }

    public void setIntendTime(LocalTime intendTime) {
        this.intendTime = intendTime;
    }

    public int getNumbersOfCustomer() {
        return numbersOfCustomer;
    }

    public void setNumbersOfCustomer(int numbersOfCustomer) {
        this.numbersOfCustomer = numbersOfCustomer;
    }

    public Set<TableRestaurant> getTableRestaurants() {
        return tableRestaurants;
    }

    public void setTableRestaurants(Set<TableRestaurant> tableRestaurants) {
        this.tableRestaurants = tableRestaurants;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public SCHEDULE_STATUS getStatus() {
        return status;
    }

    public void setStatus(SCHEDULE_STATUS status) {
        this.status = status;
    }

    LocalDate bookedDate;
    LocalTime time;
    double deposit;
    LocalTime intendTime;
    int numbersOfCustomer;
    @ManyToMany
    Set<TableRestaurant> tableRestaurants;
    @ManyToOne
    Restaurant restaurant;
    @Enumerated(EnumType.STRING)
    SCHEDULE_STATUS status;
}
