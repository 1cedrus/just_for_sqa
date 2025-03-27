package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "role",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Account> accounts = new HashSet<>();
    @OneToMany(mappedBy = "role",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @ToString.Exclude
    private Set<Employee> employees;
    public void assignAccount(Account account){
        this.accounts.add(account);
        account.setRole(this);
    }
    public void assginEmployee(Employee employee){
        this.employees.add(employee);
        employee.setRole(this);
    }
}
