package com.restaurent.manager.repository;


import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "123456";
    private static final String INVALID_EMAIL = "invalid@example.com";
    private static final String INVALID_PHONE = "000000";
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    private Role adminRole, userRole;

    void setUp() {
        adminRole = roleRepository.save(Role.builder()
            .name("ADMIN")
            .description("Admin role")
            .build());

        userRole = roleRepository.save(Role.builder()
            .name("USER")
            .description("User role")
            .build());

        accountRepository.save(Account.builder()
            .username("john_doe")
            .email("john@example.com")
            .phoneNumber("123456")
            .password("pass")
            .status(true)
            .role(adminRole)
            .build());

        accountRepository.save(Account.builder()
            .username("jane_doe")
            .email("jane@example.com")
            .phoneNumber("654321")
            .password("pass")
            .status(true)
            .role(adminRole)
            .build());

        accountRepository.save(Account.builder()
            .username("user_test")
            .email("user@example.com")
            .phoneNumber("999999")
            .password("pass")
            .status(true)
            .role(userRole)
            .build());
    }

    //ACCR1
    @Test
    @DisplayName("Should return true when account with given email and status exists")
    void existsByEmailAndStatus_shouldReturnTrue_WhenAccountExists() {
        // Arrange
        Account account = Account.builder()
            .email("test@example.com")
            .username("user1")
            .phoneNumber("1234567890")
            .password("pass")
            .status(true)
            .build();

        accountRepository.save(account);

        // Act
        boolean exists = accountRepository.existsByEmailAndStatus("test@example.com", true);

        // Assert
        assertThat(exists).isTrue();
    }

    //ACCR2
    @Test
    @DisplayName("Should return false when account with given email does not exist")
    void existsByEmailAndStatus_shouldReturnFalse_WhenEmailNotFound() {
        // Act
        boolean exists = accountRepository.existsByEmailAndStatus("notfound@example.com", true);

        // Assert
        assertThat(exists).isFalse();
    }

    //ACCR3
    @Test
    @DisplayName("Should return true when account with given phone number and status exists")
    void existsByPhoneNumberAndStatus_shouldReturnTrue_WhenAccountExists() {
        // Arrange
        Account account = Account.builder()
            .email("phoneuser@example.com")
            .username("phoneuser1")
            .phoneNumber("0987654321")
            .password("pass")
            .status(true)
            .build();

        accountRepository.save(account);

        // Act
        boolean exists = accountRepository.existsByPhoneNumberAndStatus("0987654321", true);

        // Assert
        assertThat(exists).isTrue();
    }

    //ACCR4
    @Test
    @DisplayName("Should return false when account with given phone number does not exist")
    void existsByPhoneNumberAndStatus_shouldReturnFalse_WhenPhoneNumberNotFound() {
        // Act
        boolean exists = accountRepository.existsByPhoneNumberAndStatus("0000000000", true);

        // Assert
        assertThat(exists).isFalse();
    }

    //ACCR5
    @Test
    @DisplayName("Should return account when email and status match")
    void findByEmailAndStatus_shouldReturnAccount_WhenMatch() {
        // Arrange
        Account account = Account.builder()
            .email("found@example.com")
            .username("foundUser")
            .phoneNumber("123456789")
            .password("pass")
            .status(true)
            .build();
        accountRepository.save(account);

        // Act
        Optional<Account> result = accountRepository.findByEmailAndStatus("found@example.com", true);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("found@example.com");
        assertThat(result.get().isStatus()).isTrue();
    }

    //ACCR6
    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmailAndStatus_shouldReturnEmpty_WhenEmailNotFound() {
        // Act
        Optional<Account> result = accountRepository.findByEmailAndStatus("notfound@example.com", true);

        // Assert
        assertThat(result).isEmpty();
    }

    //ACCR7
    @Test
    @DisplayName("Should return account when email exists")
    void findByEmail_shouldReturnAccount_WhenEmailExists() {
        // Arrange
        Account account = Account.builder()
            .email("exist@example.com")
            .username("existUser")
            .phoneNumber("1234567890")
            .password("password")
            .status(true)
            .build();
        accountRepository.save(account);

        // Act
        Optional<Account> result = accountRepository.findByEmail("exist@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("exist@example.com");
    }

    //ACCR8
    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmail_shouldReturnEmpty_WhenEmailNotFound() {
        // Act
        Optional<Account> result = accountRepository.findByEmail("notfound@example.com");

        // Assert
        assertThat(result).isEmpty();
    }

    //ACCR9
    @Test
    void testFindByRoleIdAndUsernameContaining_shouldReturnMatchingAccounts() {
        setUp();
        Pageable pageable = PageRequest.of(0, 10);
        List<Account> results = accountRepository.findByRole_IdAndUsernameContaining(adminRole.getId(), "doe", pageable);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Account::getUsername)
            .containsExactlyInAnyOrder("john_doe", "jane_doe");
    }

    //ACCR10
    @Test
    void testFindByRoleIdAndUsernameContaining_shouldReturnEmptyWhenRoleDoesNotMatch() {
        setUp();
        Pageable pageable = PageRequest.of(0, 10);
        List<Account> results = accountRepository.findByRole_IdAndUsernameContaining(999L, "doe", pageable);

        assertThat(results).isEmpty();
    }

    //ACCR11
    @Test
    void testFindByRoleIdAndUsernameContaining_shouldReturnOnlyMatchingRole() {
        setUp();
        Pageable pageable = PageRequest.of(0, 10);
        List<Account> results = accountRepository.findByRole_IdAndUsernameContaining(userRole.getId(), "user", pageable);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("user_test");
    }

    //ACCR12
    @Test
    void testCountByRoleIdAndUsernameContaining_shouldReturnCorrectCount() {
        setUp();
        int count = accountRepository.countByRole_IdAndUsernameContaining(adminRole.getId(), "doe");
        assertThat(count).isEqualTo(2);
    }

    //ACCR13
    @Test
    void testCountByRoleIdAndUsernameContaining_shouldReturnZeroWhenNoUsernameMatch() {
        setUp();
        int count = accountRepository.countByRole_IdAndUsernameContaining(adminRole.getId(), "xyz");
        assertThat(count).isZero();
    }

    //ACCR14
    @Test
    void testCountByRoleIdAndUsernameContaining_shouldReturnZeroWhenNoMatchingRole() {
        setUp();
        int count = accountRepository.countByRole_IdAndUsernameContaining(999L, "doe");
        assertThat(count).isZero();
    }

    //ACCR15
    @Test
    void testFindByPhoneNumber_shouldReturnAccountWhenExists() {
        setUp();
        Optional<Account> result = accountRepository.findByPhoneNumber(VALID_PHONE);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
    }

    //ACCR16
    @Test
    void testFindByPhoneNumber_shouldReturnEmptyWhenNotExists() {
        setUp();
        Optional<Account> result = accountRepository.findByPhoneNumber(INVALID_PHONE);

        assertThat(result).isEmpty();
    }

    //ACCR17
    @Test
    void testFindByEmailAndPhoneNumber_shouldReturnAccountWhenBothMatch() {
        setUp();
        Optional<Account> result = accountRepository.findByEmailAndPhoneNumber(VALID_EMAIL, VALID_PHONE);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
    }

    //ACCR18
    @Test
    void testFindByEmailAndPhoneNumber_shouldReturnEmptyWhenEmailCorrectPhoneWrong() {
        setUp();
        Optional<Account> result = accountRepository.findByEmailAndPhoneNumber(VALID_EMAIL, INVALID_PHONE);

        assertThat(result).isEmpty();
    }

    //ACCR19
    @Test
    void testFindByEmailAndPhoneNumber_shouldReturnEmptyWhenEmailWrongPhoneCorrect() {
        setUp();
        Optional<Account> result = accountRepository.findByEmailAndPhoneNumber(INVALID_EMAIL, VALID_PHONE);

        assertThat(result).isEmpty();
    }

    //ACCR20
    @Test
    void testFindByEmailAndPhoneNumber_shouldReturnEmptyWhenBothWrong() {
        setUp();
        Optional<Account> result = accountRepository.findByEmailAndPhoneNumber(INVALID_EMAIL, INVALID_PHONE);

        assertThat(result).isEmpty();
    }


}



