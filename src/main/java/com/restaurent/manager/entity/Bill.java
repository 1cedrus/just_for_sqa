package com.restaurent.manager.entity;

import com.restaurent.manager.enums.MethodPayment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    Order order;
    double total;
    double pointUsed;
    LocalDateTime dateCreated;
    @Enumerated(EnumType.STRING)
    MethodPayment methodPayment;

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setPointUsed(double pointUsed) {
        this.pointUsed = pointUsed;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setMethodPayment(MethodPayment methodPayment) {
        this.methodPayment = methodPayment;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public double getTotal() {
        return total;
    }

    public double getPointUsed() {
        return pointUsed;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public MethodPayment getMethodPayment() {
        return methodPayment;
    }
}
