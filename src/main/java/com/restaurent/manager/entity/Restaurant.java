package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String restaurantName;
    private String address;
    private String province;
    private String district;
    @ManyToOne(fetch = FetchType.LAZY)
    private Package restaurantPackage;
    private LocalDateTime expiryDate;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;
    @OneToMany(mappedBy = "restaurant",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<Employee> employees;
    @OneToMany(mappedBy = "restaurant",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Customer> customers;
    @OneToMany(mappedBy = "restaurant",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Area> areas;
    private double moneyToPoint;
    private double pointToMoney;
    private int monthsRegister;
    private String BANK_ID;
    private String ACCOUNT_NO;
    private String ACCOUNT_NAME;
    private boolean isVatActive;
    private LocalDate dateCreated;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vat_id", referencedColumnName = "id")
    private Vat vat;
    public void addEmployee(Employee employee){
        this.employees.add(employee);
        employee.setRestaurant(this);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant )) return false;
        return id != null && id.equals(((Restaurant) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
