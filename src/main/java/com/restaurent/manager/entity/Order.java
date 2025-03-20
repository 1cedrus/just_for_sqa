package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "order_restaurant")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    Employee employee;
    @ManyToOne(fetch = FetchType.EAGER)
    Customer customer;
    @ManyToOne(fetch = FetchType.EAGER)
    TableRestaurant tableRestaurant;
    LocalDate orderDate;
    @OneToOne(mappedBy = "order")
    Bill bill;
    @OneToMany(mappedBy = "order",fetch = FetchType.EAGER,cascade = CascadeType.ALL
    )
    Set<DishOrder> dishOrders = new HashSet<>();
    @ManyToOne(fetch = FetchType.EAGER)
    Restaurant restaurant;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        return id != null && id.equals(((Order) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
