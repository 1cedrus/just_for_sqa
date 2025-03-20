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

    public void setId(Long id) {
        this.id = id;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setRestaurantPackage(Package restaurantPackage) {
        this.restaurantPackage = restaurantPackage;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public void setCustomers(Set<Customer> customers) {
        this.customers = customers;
    }

    public void setAreas(Set<Area> areas) {
        this.areas = areas;
    }

    public void setMoneyToPoint(double moneyToPoint) {
        this.moneyToPoint = moneyToPoint;
    }

    public void setPointToMoney(double pointToMoney) {
        this.pointToMoney = pointToMoney;
    }

    public void setMonthsRegister(int monthsRegister) {
        this.monthsRegister = monthsRegister;
    }

    public void setBANK_ID(String BANK_ID) {
        this.BANK_ID = BANK_ID;
    }

    public void setACCOUNT_NO(String ACCOUNT_NO) {
        this.ACCOUNT_NO = ACCOUNT_NO;
    }

    public void setACCOUNT_NAME(String ACCOUNT_NAME) {
        this.ACCOUNT_NAME = ACCOUNT_NAME;
    }

    public void setVatActive(boolean vatActive) {
        isVatActive = vatActive;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setVat(Vat vat) {
        this.vat = vat;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Long getId() {
        return id;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getAddress() {
        return address;
    }

    public String getProvince() {
        return province;
    }

    public String getDistrict() {
        return district;
    }

    public Package getRestaurantPackage() {
        return restaurantPackage;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public Account getAccount() {
        return account;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public Set<Area> getAreas() {
        return areas;
    }

    public double getMoneyToPoint() {
        return moneyToPoint;
    }

    public double getPointToMoney() {
        return pointToMoney;
    }

    public int getMonthsRegister() {
        return monthsRegister;
    }

    public String getBANK_ID() {
        return BANK_ID;
    }

    public String getACCOUNT_NO() {
        return ACCOUNT_NO;
    }

    public String getACCOUNT_NAME() {
        return ACCOUNT_NAME;
    }

    public boolean isVatActive() {
        return isVatActive;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public Vat getVat() {
        return vat;
    }
}
