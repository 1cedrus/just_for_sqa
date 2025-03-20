package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String phoneNumber;
    private String password;
    private String email;
    private boolean status;
    private String otp;
    private LocalDateTime otpGeneratedTime;
    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;
    @OneToOne(mappedBy = "account")
    private Restaurant restaurant;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account )) return false;
        return id != null && id.equals(((Account) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
