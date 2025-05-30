package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.restaurant.*;
import com.restaurent.manager.dto.response.RestaurantResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.RestaurantMapper;
import com.restaurent.manager.repository.*;
import com.restaurent.manager.service.impl.AccountService;
import com.restaurent.manager.service.impl.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RestaurantServiceTest {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private IPackageService packageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        restaurantRepository.deleteAll();
        accountRepository.deleteAll();
        packageRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    // --- Kiểm thử cho initRestaurant ---
    // ID: RS-2
    @Test
    void testInitRestaurant_AccountAlreadyHasRestaurant() {
        // Chuẩn bị dữ liệu
        Account account = createAccount("test@example.com", "testuser", "password");
        account = accountRepository.save(account);
        
        Restaurant existingRestaurant = createRestaurant("Existing Restaurant");
        existingRestaurant.setAccount(account);
        restaurantRepository.save(existingRestaurant);

        RestaurantRequest request = createRestaurantRequest(account.getId(), "Test Restaurant");

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> restaurantService.initRestaurant(request));
        assertEquals(ErrorCode.LIMITED_RESTAURANT, exception.getErrorCode());
    }

    // ID: RS-3
    @Test
    void testInitRestaurant_RestaurantNameExisted() {
        // Chuẩn bị dữ liệu
        Account account = createAccount("test@example.com", "testuser", "password");
        account = accountRepository.save(account);
        
        Restaurant existingRestaurant = createRestaurant("Test Restaurant");
        restaurantRepository.save(existingRestaurant);

        RestaurantRequest request = createRestaurantRequest(account.getId(), "Test Restaurant");

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> restaurantService.initRestaurant(request));
        assertEquals(ErrorCode.RESTAURANT_NAME_EXISTED, exception.getErrorCode());
    }

    // ID: RS-4
    @Test
    void testInitRestaurant_AccountNotExisted() {
        // Chuẩn bị dữ liệu
        Long nonExistentAccountId = 999L;
        RestaurantRequest request = createRestaurantRequest(nonExistentAccountId, "Test Restaurant");

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> restaurantService.initRestaurant(request));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    // ID: RS-1
    @Test
    void testInitRestaurant_Success() {
        // Chuẩn bị dữ liệu
        Role role = Role.builder().name("role").build();
        roleRepository.saveAndFlush(role);
        Account account = createAccount("test@example.com", "testuser", "password");
        account.setRole(role);
        account = accountRepository.save(account);

        Package trialPack = createPackage("TRIAL", 0.0);
        packageRepository.save(trialPack);

        RestaurantRequest request = createRestaurantRequest(account.getId(), "Test Restaurant");

        // Thực thi
        RestaurantResponse result = restaurantService.initRestaurant(request);

        // Kiểm tra
        assertNotNull(result);
        assertNotNull(result.getToken());
        
        // Verify in database
        boolean exists = restaurantRepository.existsByAccount_Id(account.getId());
        assertTrue(exists);
    }

    // --- Kiểm thử cho getRestaurants ---
    // RS-5
    @Test
    void testGetRestaurants_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Restaurant r1 = createRestaurant("Restaurant 1");
        Restaurant r2 = createRestaurant("Restaurant 2");
        restaurantRepository.save(r1);
        restaurantRepository.save(r2);

        // Thực thi
        List<RestaurantResponse> result = restaurantService.getRestaurants();

        // Kiểm tra
        assertEquals(2, result.size());
    }

    // RS-6
    @Test
    void testGetRestaurants_EmptyList() {
        // Thực thi
        List<RestaurantResponse> result = restaurantService.getRestaurants();

        // Kiểm tra
        assertTrue(result.isEmpty());
    }

    // --- Kiểm thử cho updateRestaurant (restaurantId, RestaurantUpdateRequest) ---

    // RS-7
    @Test
    void testUpdateRestaurant_WithRestaurantId_Success() {
        // Chuẩn bị dữ liệu
        Package pack = createPackage("BASIC", 100.0);
        pack = packageRepository.save(pack);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest(pack.getId(), 3);

        // Thực thi
        RestaurantResponse result = restaurantService.updateRestaurant(restaurant.getId(), request);

        // Kiểm tra
        assertEquals(restaurant.getId(), result.getId());
        
        // Verify in database
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElse(null);
        assertNotNull(updatedRestaurant);
        assertEquals(pack.getId(), updatedRestaurant.getRestaurantPackage().getId());
    }

    // --- Kiểm thử cho updateRestaurant (accountId, RestaurantManagerUpdateRequest) ---

    // RS-8
    @Test
    void testUpdateRestaurant_WithManagerRequest_Success() {
        // Chuẩn bị dữ liệu
        Account account = createAccount("test@example.com", "testuser", "password");
        account = accountRepository.save(account);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setAccount(account);
        restaurant = restaurantRepository.save(restaurant);

        RestaurantManagerUpdateRequest request = RestaurantManagerUpdateRequest.builder()
                .address("New Address")
                .restaurantName("New Name")
                .district("New District")
                .province("New Province")
                .build();

        // Thực thi
        RestaurantResponse result = restaurantService.updateRestaurant(account.getId(), request);

        // Kiểm tra
        assertEquals(restaurant.getId(), result.getId());
        
        // Verify in database
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElse(null);
        assertNotNull(updatedRestaurant);
    }

    // RS-9
    @Test
    void testUpdateRestaurant_WithManagerRequest_NotExist() {
        // Chuẩn bị dữ liệu
        Long nonExistentAccountId = 999L;
        RestaurantManagerUpdateRequest request = new RestaurantManagerUpdateRequest();

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                restaurantService.updateRestaurant(nonExistentAccountId, request));
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    // --- Kiểm thử cho updateRestaurant (accountId, RestaurantPaymentRequest) ---

    // RS-10
    @Test
    void testUpdateRestaurant_WithPaymentRequest_Success() {
        // Chuẩn bị dữ liệu
        Account account = createAccount("test@example.com", "testuser", "password");
        account = accountRepository.save(account);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setAccount(account);
        restaurant = restaurantRepository.save(restaurant);

        RestaurantPaymentRequest request = RestaurantPaymentRequest.builder()
                .ACCOUNT_NAME("Test Account")
                .ACCOUNT_NO("123456789")
                .BANK_ID("BANK001")
                .build();

        // Thực thi
        RestaurantResponse result = restaurantService.updateRestaurant(account.getId(), request);

        // Kiểm tra
        assertEquals(restaurant.getId(), result.getId());
        
        // Verify in database
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElse(null);
        assertNotNull(updatedRestaurant);
    }

    // RS-11
    @Test
    void testUpdateRestaurant_WithPaymentRequest_NotExist() {
        // Chuẩn bị dữ liệu
        Long nonExistentAccountId = 999L;
        RestaurantPaymentRequest request = new RestaurantPaymentRequest();

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                restaurantService.updateRestaurant(nonExistentAccountId, request));
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    // --- Kiểm thử cho getRestaurantById ---

    // RS-12
    @Test
    void testGetRestaurantById_Success() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        // Thực thi
        Restaurant result = restaurantService.getRestaurantById(restaurant.getId());

        // Kiểm tra
        assertEquals(restaurant.getId(), result.getId());
    }

    // RS-13
    @Test
    void testGetRestaurantById_NotExist() {
        // Chuẩn bị dữ liệu
        Long nonExistentId = 999L;

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                restaurantService.getRestaurantById(nonExistentId));
        assertEquals(ErrorCode.INVALID_KEY, exception.getErrorCode());
    }

    // --- Kiểm thử cho getRestaurantByAccountId ---

    // RS-14
    @Test
    void testGetRestaurantByAccountId_Success() {
        // Chuẩn bị dữ liệu
        Account account = createAccount("test@example.com", "testuser", "password");
        account = accountRepository.save(account);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setAccount(account);
        restaurant.setVatActive(true);
        restaurant = restaurantRepository.save(restaurant);

        // Thực thi
        RestaurantResponse result = restaurantService.getRestaurantByAccountId(account.getId());

        // Kiểm tra
        assertNotNull(result);
        assertTrue(result.isVatActive());
    }

    // RS-15
    @Test
    void testGetRestaurantByAccountId_NotExist() {
        // Chuẩn bị dữ liệu
        Long nonExistentAccountId = 999L;

        // Thực thi
        RestaurantResponse result = restaurantService.getRestaurantByAccountId(nonExistentAccountId);

        // Kiểm tra
        assertNull(result);
    }

    // --- Kiểm thử cho getMoneyToUpdatePackForRestaurant ---

    // RS-16
    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterHigh_MonthsHigh() {
        // Chuẩn bị dữ liệu
        Package currentPack = createPackageWithPrice(1000.0, 12000.0);
        currentPack = packageRepository.save(currentPack);
        
        Package newPack = createPackageWithPrice(2000.0, 24000.0);
        newPack = packageRepository.save(newPack);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(12);
        restaurant.setRestaurantPackage(currentPack);
        restaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest(newPack.getId(), 12);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurant.getId(), request);

        // Kiểm tra (12000 / 365 * 10 ≈ 328.77, 24000 - 328.77 ≈ 23671.23, làm tròn 23671)
        assertEquals(23671, result, 1);
    }

    // RS-17
    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterHigh_MonthsLow() {
        // Chuẩn bị dữ liệu
        Package currentPack = createPackageWithPrice(1000.0, 12000.0);
        currentPack = packageRepository.save(currentPack);
        
        Package newPack = createPackageWithPrice(2000.0, 24000.0);
        newPack = packageRepository.save(newPack);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(12);
        restaurant.setRestaurantPackage(currentPack);
        restaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest(newPack.getId(), 6);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurant.getId(), request);

        // Kiểm tra (12000 / 365 * 10 ≈ 328.77, 2000 * 6 - 328.77 ≈ 11671.23, làm tròn 11671)
        assertEquals(11671, result, 1);
    }

    // RS-18
    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterLow_MonthsHigh() {
        // Chuẩn bị dữ liệu
        Package currentPack = createPackageWithPrice(1000.0, 12000.0);
        currentPack = packageRepository.save(currentPack);
        
        Package newPack = createPackageWithPrice(2000.0, 24000.0);
        newPack = packageRepository.save(newPack);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(1);
        restaurant.setRestaurantPackage(currentPack);
        restaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest(newPack.getId(), 12);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurant.getId(), request);

        // Kiểm tra (1000 / 30 * 10 ≈ 333.33, 24000 - 333.33 ≈ 23666.67, làm tròn 23667)
        assertEquals(23667, result, 1);
    }

    // RS-19
    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterLow_MonthsLow() {
        // Chuẩn bị dữ liệu
        Package currentPack = createPackageWithPrice(1000.0, 12000.0);
        currentPack = packageRepository.save(currentPack);
        
        Package newPack = createPackageWithPrice(2000.0, 24000.0);
        newPack = packageRepository.save(newPack);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(1);
        restaurant.setRestaurantPackage(currentPack);
        restaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest(newPack.getId(), 6);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurant.getId(), request);

        // Kiểm tra (1000 / 30 * 10 ≈ 333.33, 2000 * 6 - 333.33 ≈ 11666.67, làm tròn 11667)
        assertEquals(11667, result, 1);
    }

    // RS-20
    @Test
    void testGetMoneyToUpdatePack_DayLeftZero_MonthsHigh() {
        // Chuẩn bị dữ liệu
        Package newPack = createPackageWithPrice(2000.0, 24000.0);
        newPack = packageRepository.save(newPack);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setExpiryDate(LocalDateTime.now().minusDays(1));
        restaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest(newPack.getId(), 12);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurant.getId(), request);

        // Kiểm tra (24000, làm tròn 24000)
        assertEquals(24000, result, 1);
    }

    // RS-21
    @Test
    void testGetMoneyToUpdatePack_DayLeftZero_MonthsLow() {
        // Chuẩn bị dữ liệu
        Package newPack = createPackageWithPrice(2000.0, 24000.0);
        newPack = packageRepository.save(newPack);

        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant.setExpiryDate(LocalDateTime.now().minusDays(1));
        restaurant = restaurantRepository.save(restaurant);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest(newPack.getId(), 6);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurant.getId(), request);

        // Kiểm tra (2000 * 6 = 12000, làm tròn 12000)
        assertEquals(12000, result, 1);
    }

    // --- Kiểm thử cho updateRestaurantVatById ---

    // RS-22
    @Test
    void testUpdateRestaurantVatById_Success() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        // Thực thi
        restaurantService.updateRestaurantVatById(restaurant.getId(), true);

        // Kiểm tra
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElse(null);
        assertNotNull(updatedRestaurant);
        assertTrue(updatedRestaurant.isVatActive());
    }

    // --- Kiểm thử cho updatePointForRestaurant ---

    // RS-23
    @Test
    void testUpdatePointForRestaurant_Success() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        PointsRequest request = createPointsRequest(200000, 2000);

        // Thực thi
        RestaurantResponse result = restaurantService.updatePointForRestaurant(restaurant.getId(), request);

        // Kiểm tra
        assertEquals(restaurant.getId(), result.getId());
        
        // Verify in database
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElse(null);
        assertNotNull(updatedRestaurant);
    }

    // --- Kiểm thử cho countRestaurantByDateCreated ---

    // RS-24
    @Test
    void testCountRestaurantByDateCreated() {
        // Chuẩn bị dữ liệu
        LocalDate date = LocalDate.now();
        
        Restaurant r1 = createRestaurant("Restaurant 1");
        r1.setDateCreated(date);
        restaurantRepository.save(r1);
        
        Restaurant r2 = createRestaurant("Restaurant 2");
        r2.setDateCreated(date);
        restaurantRepository.save(r2);

        // Thực thi
        int result = restaurantService.countRestaurantByDateCreated(date);

        // Kiểm tra
        assertEquals(2, result);
    }

    // Helper methods để tạo đối tượng test
    private RestaurantRequest createRestaurantRequest(Long accountId, String name) {
        return RestaurantRequest.builder()
                .accountId(accountId)
                .restaurantName(name)
                .build();
    }

    private Account createAccount(String email, String username, String password) {
        Account account = new Account();
        account.setEmail(email);
        account.setUsername(username);
        account.setPassword(password);
        return account;
    }

    private Restaurant createRestaurant(String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName(name);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(30));
        restaurant.setDateCreated(LocalDate.now());
        return restaurant;
    }

    private Package createPackage(String name, double pricePerMonth) {
        Package pack = new Package();
        pack.setPackName(name);
        pack.setPricePerMonth(pricePerMonth);
        pack.setPricePerYear(pricePerMonth * 12);
        pack.setPermissions(new HashSet<>());
        return pack;
    }

    private Package createPackageWithPrice(double pricePerMonth, double pricePerYear) {
        Package pack = new Package();
        pack.setPricePerMonth(pricePerMonth);
        pack.setPricePerYear(pricePerYear);
        pack.setPermissions(new HashSet<>());
        return pack;
    }

    private PointsRequest createPointsRequest(double moneyToPoint, double pointToMoney) {
        return PointsRequest.builder()
                .moneyToPoint(moneyToPoint)
                .pointToMoney(pointToMoney)
                .build();
    }
}
