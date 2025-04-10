package com.restaurent.manager.service;


import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.AccountRequest;
import com.restaurent.manager.dto.request.AuthenticationRequest;
import com.restaurent.manager.dto.request.VerifyAccount;
import com.restaurent.manager.dto.response.AccountResponse;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.VerifyResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.enums.RoleSystem;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.AccountMapper;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.service.impl.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    AccountMapper accountmapper;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private IEmailService emailService;
    @Mock
    private IRoleService roleService;
    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(accountService, "signerKey", "testsignerkey_32_bytes_long_string");
    }

    @Test
    void register_Success() {
        // Arrange
        AccountRequest request = AccountRequest.builder()
            .username("john_doe")
            .phoneNumber("0312345678")
            .password("StrongPass123")
            .email("john@example.com")
            .build();

        when(accountRepository.existsByEmailAndStatus(request.getEmail(), true)).thenReturn(false);
        when(accountRepository.existsByPhoneNumberAndStatus(request.getPhoneNumber(), true)).thenReturn(false);
        when(accountRepository.existsByEmailAndStatus(request.getEmail(), false)).thenReturn(false);

        String generatedOtp = "123456";
        when(emailService.generateCode(6)).thenReturn(generatedOtp);

        Role mockRole = Role.builder().name(RoleSystem.MANAGER.name()).build();
        when(roleService.findByRoleName(RoleSystem.MANAGER.name())).thenReturn(mockRole);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            return account;
        });

        // Act
        AccountResponse response = accountService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(emailService, times(1)).sendEmail(eq(request.getEmail()), anyString(), eq("Verify account "));
    }

    @Test
    void register_existsByEmailAndStatus() {
        // accountRepository.existsByEmailAndStatus(req.getEmail(), true)
        AccountRequest request = AccountRequest.builder()
            .username("testUser")
            .phoneNumber("0312345678")
            .password("Password123")
            .email("test@example.com")
            .build();

        when(accountRepository.existsByEmailAndStatus(request.getEmail(), true)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> accountService.register(request));
        assertEquals(ErrorCode.EMAIL_EXIST, exception.getErrorCode());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void register_existsByPhoneNumberAndStatus() {
        // accountRepository.existsByPhoneNumberAndStatus(req.getPhoneNumber(), true)
        AccountRequest request = AccountRequest.builder()
            .username("testUser")
            .phoneNumber("0312345678")
            .password("Password123")
            .email("test@example.com")
            .build();

        when(accountRepository.existsByEmailAndStatus(request.getEmail(), true)).thenReturn(false);
        when(accountRepository.existsByPhoneNumberAndStatus(request.getPhoneNumber(), true)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> accountService.register(request));
        assertEquals(ErrorCode.PHONENUMBER_EXIST, exception.getErrorCode());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountsManager_NoResults() {
        Pageable pageable = PageRequest.of(0, 10);
        String query = "";

        when(accountRepository.findByRole_IdAndUsernameContaining(5L, query, pageable))
            .thenReturn(Collections.emptyList());
        when(accountRepository.countByRole_IdAndUsernameContaining(5L, query))
            .thenReturn((int) 0L);

        PagingResult<AccountResponse> result = accountService.getAccountsManager(pageable, query);

        assertNotNull(result);
        assertEquals(0, result.getResults().size());
        assertEquals(0, result.getTotalItems());
    }

    @Test
    void getAccountsManager_OneResult() {
        Pageable pageable = PageRequest.of(0, 10);
        String query = "john";

        Account account = Account.builder().id(1L).username("john").build();
        AccountResponse response = AccountResponse.builder().id(1L).username("john").build();

        when(accountRepository.findByRole_IdAndUsernameContaining(5L, query, pageable))
            .thenReturn(List.of(account));
        when(accountRepository.countByRole_IdAndUsernameContaining(5L, query))
            .thenReturn((int) 1L);
        when(accountMapper.toAccountResponse(account)).thenReturn(response);

        PagingResult<AccountResponse> result = accountService.getAccountsManager(pageable, query);

        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals("john", result.getResults().get(0).getUsername());
        assertEquals(1, result.getTotalItems());
    }

    @Test
    void getAccountsManager_MultipleResults() {
        Pageable pageable = PageRequest.of(0, 10);
        String query = "manager";

        Account account1 = Account.builder().id(1L).username("manager1").build();
        Account account2 = Account.builder().id(2L).username("manager2").build();

        AccountResponse response1 = AccountResponse.builder().id(1L).username("manager1").build();
        AccountResponse response2 = AccountResponse.builder().id(2L).username("manager2").build();

        when(accountRepository.findByRole_IdAndUsernameContaining(5L, query, pageable))
            .thenReturn(List.of(account1, account2));
        when(accountRepository.countByRole_IdAndUsernameContaining(5L, query))
            .thenReturn((int) 2L);
        when(accountMapper.toAccountResponse(account1)).thenReturn(response1);
        when(accountMapper.toAccountResponse(account2)).thenReturn(response2);

        PagingResult<AccountResponse> result = accountService.getAccountsManager(pageable, query);

        assertNotNull(result);
        assertEquals(2, result.getResults().size());
        assertEquals(2, result.getTotalItems());
        assertEquals("manager1", result.getResults().get(0).getUsername());
        assertEquals("manager2", result.getResults().get(1).getUsername());
    }

    @Test
    void verifyAccount_userNotExist() {
        VerifyAccount request = new VerifyAccount("notfound@example.com", "123456");

        when(accountRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
            () -> accountService.verifyAccount(request));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void verifyAccount_userAlreadyVerified() {
        VerifyAccount request = new VerifyAccount("john@example.com", "123456");
        Account existingAccount = Account.builder()
            .email("john@example.com")
            .otp("123456")
            .otpGeneratedTime(LocalDateTime.now())
            .status(true)
            .build();

        when(accountRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(existingAccount));

        AppException exception = assertThrows(AppException.class,
            () -> accountService.verifyAccount(request));

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
    }

    @Test
    void verifyAccount_success() {
        VerifyAccount request = new VerifyAccount("jane@example.com", "123456");
        Account account = Account.builder()
            .email("jane@example.com")
            .otp("123456")
            .otpGeneratedTime(LocalDateTime.now().minusSeconds(30))
            .status(false)
            .build();

        Account savedAccount = Account.builder()
            .email("jane@example.com")
            .otp("123456")
            .otpGeneratedTime(account.getOtpGeneratedTime())
            .status(true)
            .build();

        when(accountRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
            .thenReturn(savedAccount);

        VerifyResponse response = accountService.verifyAccount(request);

        assertTrue(response.isStatus());
        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    void verifyAccount_wrongOtpOrExpired() {
        VerifyAccount request = new VerifyAccount("alice@example.com", "wrongOtp");
        Account account = Account.builder()
            .email("alice@example.com")
            .otp("correctOtp")
            .otpGeneratedTime(LocalDateTime.now().minusSeconds(70))
            .status(false)
            .build();

        when(accountRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(account));

        VerifyResponse response = accountService.verifyAccount(request);

        assertFalse(response.isStatus());
        assertEquals("alice@example.com", response.getEmail());
    }

    @Test
    void authenticated_userNotFound() {
        AuthenticationRequest request = new AuthenticationRequest("notfound@example.com", "password123");

        when(accountRepository.findByEmailAndStatus(request.getEmail(), true))
            .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
            () -> accountService.authenticated(request));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void authenticated_wrongPassword() {
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "wrongPassword");
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);

        Account account = Account.builder()
            .email("user@example.com")
            .password(encoder.encode("correctPassword"))
            .status(true)
            .build();

        when(accountRepository.findByEmailAndStatus(request.getEmail(), true))
            .thenReturn(Optional.of(account));

        AppException exception = assertThrows(AppException.class,
            () -> accountService.authenticated(request));

        assertEquals(ErrorCode.PASSWORD_INCORRECT, exception.getErrorCode());
    }

    @Test
    void authenticated_success() {
        AuthenticationRequest request = new AuthenticationRequest("authuser@example.com", "validPassword");
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);

        Account account = Account.builder()
            .id(1L)
            .email("authuser@example.com")
            .username("authuser")
            .password(encoder.encode("validPassword"))
            .status(true)
            .role(Role.builder().name("MANAGER").build())
            .build();

        when(accountRepository.findByEmailAndStatus(request.getEmail(), true))
            .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
            .thenReturn(account);

        AuthenticationResponse response = accountService.authenticated(request);

        assertTrue(response.isAuthenticated());
        assertNotNull(response.getToken());
    }

    @Test
    void verifyOtp_Success() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";
        VerifyAccount request = VerifyAccount.builder()
            .email(email)
            .otp(otp)
            .build();

        Account account = Account.builder()
            .email(email)
            .otp(otp)
            .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        ReflectionTestUtils.setField(accountService, "signerKey", "test_signer_key_that_is_32_chars!");

        // Act
        AuthenticationResponse response = accountService.verifyOtp(request);

        // Assert
        assertTrue(response.isAuthenticated());
        assertNotNull(response.getToken());
    }

    @Test
    void verifyOtp_InvalidOtp() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";
        VerifyAccount request = VerifyAccount.builder()
            .email(email)
            .otp(otp)
            .build();

        Account account = Account.builder()
            .email(email)
            .otp("654321") // Incorrect stored OTP
            .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));

        // Act
        AuthenticationResponse response = accountService.verifyOtp(request);

        // Assert
        assertFalse(response.isAuthenticated());
        assertNull(response.getToken());
    }

    @Test
    void verifyOtp_EmailNotFound() {
        // Arrange
        String email = "notfound@example.com";
        VerifyAccount request = VerifyAccount.builder()
            .email(email)
            .otp("123456")
            .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> accountService.verifyOtp(request));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void sendOtp_success() {
        // Arrange
        String email = "user@example.com";
        String generatedOtp = "123456";

        Account account = new Account();
        account.setEmail(email);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(emailService.generateCode(6)).thenReturn(generatedOtp);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        String otpResult = accountService.sendOtp(email);

        // Assert
        assertEquals(generatedOtp, otpResult);
        verify(accountRepository).save(account);
        verify(emailService).sendEmail(eq(email), contains(generatedOtp), eq("Verify Account"));
    }

    @Test
    void sendOtp_emailNotFound_shouldThrowException() {
        // Arrange
        String email = "notfound@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> accountService.sendOtp(email));
        assertEquals(ErrorCode.EMAIL_NOT_EXIST, exception.getErrorCode());

        verify(accountRepository, never()).save(any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void authenticatedEmail_shouldReturnAuthenticatedTrue() {
        // Arrange
        String email = "user@example.com";

        // Act
        AuthenticationResponse response = accountService.authenticatedEmail(email);

        // Assert
        assertNotNull(response);
        assertTrue(response.isAuthenticated());
    }

    @Test
    void regenerateOtp_success() {
        // Arrange
        String email = "user@example.com";
        Account account = Account.builder()
            .email(email)
            .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(emailService.generateCode(6)).thenReturn("123456");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = accountService.regenerateOtp(email);

        // Assert
        assertEquals("New otp are generated !", result);
        assertEquals("123456", account.getOtp());
        assertNotNull(account.getOtpGeneratedTime());
        verify(accountRepository).save(account);
        verify(emailService).sendEmail(eq(email), anyString(), eq("Verify account "));
    }

    @Test
    void regenerateOtp_shouldThrowException_whenEmailNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> accountService.regenerateOtp(email));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void findAccountByID_success() {
        // Arrange
        Long accountId = 1L;
        Account expectedAccount = Account.builder()
            .id(accountId)
            .email("test@example.com")
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(expectedAccount));

        // Act
        Account result = accountService.findAccountByID(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAccount.getId(), result.getId());
        assertEquals(expectedAccount.getEmail(), result.getEmail());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void findAccountByID_shouldThrowException_whenAccountNotFound() {
        // Arrange
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> accountService.findAccountByID(accountId));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void getAccountById_success() {
        // Arrange
        Long accountId = 1L;
        Account account = Account.builder()
            .id(accountId)
            .username("john_doe")
            .email("john@example.com")
            .build();

        AccountResponse expectedResponse = AccountResponse.builder()
            .id(accountId)
            .username("john_doe")
            .email("john@example.com")
            .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountmapper.toAccountResponse(account)).thenReturn(expectedResponse);

        // Act
        AccountResponse actualResponse = accountService.getAccountById(accountId);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
        verify(accountRepository).findById(accountId);
        verify(accountmapper).toAccountResponse(account);
    }

    @Test
    void getAccountById_shouldThrowException_whenAccountNotFound() {
        // Arrange
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> accountService.getAccountById(accountId));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository).findById(accountId);
        verify(accountMapper, never()).toAccountResponse(any());
    }

    @Test
    void findAccountByPhoneNumber_success() {
        // Arrange
        String phoneNumber = "0987654321";
        Account account = Account.builder()
            .id(1L)
            .email("test@example.com")
            .phoneNumber(phoneNumber)
            .username("testUser")
            .build();

        when(accountRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(account));

        // Act
        Account result = accountService.findAccountByPhoneNumber(phoneNumber);

        // Assert
        assertNotNull(result);
        assertEquals(account.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(account.getEmail(), result.getEmail());
        verify(accountRepository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void findAccountByPhoneNumber_shouldThrowException_whenNotFound() {
        // Arrange
        String phoneNumber = "0987654321";
        when(accountRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.findAccountByPhoneNumber(phoneNumber);
        });

        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
        verify(accountRepository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void generateToken_success() {
        // Arrange
        Account user = Account.builder()
            .id(1L)
            .email("test@example.com")
            .username("testUser")
            .role(Role.builder().name("ADMIN").build())
            .build();

        ReflectionTestUtils.setField(accountService, "signerKey", "12345678901234567890123456789012"); // 256-bit key

        // Act
        String token = accountService.generateToken(user);

        // Assert
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // Basic structure check
    }

    @Test
    void buildScope_withRoleAndRestaurantWithPermissions_returnsFullScope() {
        // Given
        Permission p1 = new Permission();
        p1.setName("CREATE_MENU");

        Permission p2 = new Permission();
        p2.setName("VIEW_STATS");

        Package restaurantPackage = new Package();
        restaurantPackage.setPermissions(Set.of(p1, p2));

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantPackage(restaurantPackage);

        Role role = new Role();
        role.setName("MANAGER");

        Account account = Account.builder()
            .role(role)
            .restaurant(restaurant)
            .build();

        // When
        String scope = accountService.buildScope(account);

        // Then
        assertTrue(scope.contains("ROLE_MANAGER"));
        assertTrue(scope.contains("CREATE_MENU"));
        assertTrue(scope.contains("VIEW_STATS"));
    }

    @Test
    void buildScope_withOnlyRole_returnsOnlyRole() {
        Role role = new Role();
        role.setName("ADMIN");

        Account account = Account.builder()
            .role(role)
            .restaurant(null)
            .build();

        String scope = accountService.buildScope(account);

        assertEquals("ROLE_ADMIN", scope);
    }

    @Test
    void buildScope_withNullRole_returnsEmptyScope() {
        Account account = Account.builder()
            .role(null)
            .restaurant(null)
            .build();

        String scope = accountService.buildScope(account);

        assertEquals("", scope);
    }

    @Test
    void buildScope_withNullRestaurantPackage_returnsOnlyRole() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantPackage(null);

        Role role = new Role();
        role.setName("STAFF");

        Account account = Account.builder()
            .role(role)
            .restaurant(restaurant)
            .build();

        String scope = accountService.buildScope(account);

        assertEquals("ROLE_STAFF", scope);
    }

    @Test
    void buildScope_withEmptyPermissionSet_returnsOnlyRole() {
        Package restaurantPackage = new Package();
        restaurantPackage.setPermissions(Collections.emptySet());

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantPackage(restaurantPackage);

        Role role = new Role();
        role.setName("OWNER");

        Account account = Account.builder()
            .role(role)
            .restaurant(restaurant)
            .build();

        String scope = accountService.buildScope(account);

        assertEquals("ROLE_OWNER", scope);
    }
}

