package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByEmailAndStatus(String email,boolean status);
    boolean existsByPhoneNumberAndStatus(String phoneNumber,boolean status);

    Optional<Account> findByEmailAndStatus(String email,boolean status);
    Optional<Account> findByEmail(String userName);
    List<Account> findByRole_IdAndUsernameContaining(Long roleId, String query, Pageable pageable);
    int countByRole_IdAndUsernameContaining(Long roleId, String query);
    Optional<Account> findByPhoneNumber(String phoneNumber);
    Optional<Account> findByEmailAndPhoneNumber(String email, String phoneNumber);
}
