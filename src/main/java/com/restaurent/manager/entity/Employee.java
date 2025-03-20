package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String username;
    String password;
    String employeeName;
    String phoneNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    Restaurant restaurant;
    @ManyToOne(fetch = FetchType.LAZY)
    Role role;
    @OneToMany(mappedBy = "employee",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
    Set<Order> orders;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee )) return false;
        return id != null && id.equals(((Employee) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
