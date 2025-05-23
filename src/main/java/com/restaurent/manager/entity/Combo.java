package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Combo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    double price;
    String description;
    String imageUrl;
    boolean status;
    @ManyToMany(fetch = FetchType.EAGER)
    Set<Dish> dishes = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    Account account;
    @ManyToOne(fetch = FetchType.LAZY)
    Restaurant restaurant;
}
