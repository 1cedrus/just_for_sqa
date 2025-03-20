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
}
