package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.restaurant.*;
import com.restaurent.manager.dto.response.RestaurantResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.RestaurantMapper;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.service.impl.AccountService;
import com.restaurent.manager.service.impl.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantMapper restaurantMapper;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private IPackageService packageService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
    }

    // --- Kiểm thử cho initRestaurant ---

    @Test
    void testInitRestaurant_AccountAlreadyHasRestaurant() {
        // Chuẩn bị dữ liệu
        RestaurantRequest request = createRestaurantRequest(1L, "Test Restaurant");
        when(restaurantRepository.existsByAccount_Id(1L)).thenReturn(true);

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> restaurantService.initRestaurant(request));
        assertEquals(ErrorCode.LIMITED_RESTAURANT, exception.getErrorCode());
        verify(restaurantRepository, times(1)).existsByAccount_Id(1L);
        verifyNoMoreInteractions(restaurantRepository, accountRepository, restaurantMapper, packageService);
    }

    @Test
    void testInitRestaurant_RestaurantNameExisted() {
        // Chuẩn bị dữ liệu
        RestaurantRequest request = createRestaurantRequest(1L, "Test Restaurant");
        when(restaurantRepository.existsByAccount_Id(1L)).thenReturn(false);
        when(restaurantRepository.existsByRestaurantName("Test Restaurant")).thenReturn(true);

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> restaurantService.initRestaurant(request));
        assertEquals(ErrorCode.RESTAURANT_NAME_EXISTED, exception.getErrorCode());
        verify(restaurantRepository, times(1)).existsByAccount_Id(1L);
        verify(restaurantRepository, times(1)).existsByRestaurantName("Test Restaurant");
    }

    @Test
    void testInitRestaurant_AccountNotExisted() {
        // Chuẩn bị dữ liệu
        RestaurantRequest request = createRestaurantRequest(1L, "Test Restaurant");
        when(restaurantRepository.existsByAccount_Id(1L)).thenReturn(false);
        when(restaurantRepository.existsByRestaurantName("Test Restaurant")).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () -> restaurantService.initRestaurant(request));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void testInitRestaurant_Success() {
        // Chuẩn bị dữ liệu
        RestaurantRequest request = createRestaurantRequest(1L, "Test Restaurant");
        Account account = createAccount(1L);
        Restaurant restaurant = createRestaurant(1L);
        RestaurantResponse response = createRestaurantResponse(1L, "token");
        Package pack = createPackage("TRIAL");

        when(restaurantRepository.existsByAccount_Id(1L)).thenReturn(false);
        when(restaurantRepository.existsByRestaurantName("Test Restaurant")).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(restaurantMapper.toRestaurant(request)).thenReturn(restaurant);
        when(packageService.findByPackName("TRIAL")).thenReturn(pack);
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantResponse(restaurant)).thenReturn(response);
        when(accountService.generateToken(account)).thenReturn("token");

        // Thực thi
        RestaurantResponse result = restaurantService.initRestaurant(request);

        // Kiểm tra
        assertEquals("token", result.getToken());
        verify(restaurantRepository, times(1)).save(restaurant);
        verify(accountService, times(1)).generateToken(account);
    }

    // --- Kiểm thử cho getRestaurants ---

    @Test
    void testGetRestaurants_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Restaurant r1 = createRestaurant(1L);
        Restaurant r2 = createRestaurant(2L);
        List<Restaurant> restaurants = Arrays.asList(r1, r2);
        when(restaurantRepository.findAll()).thenReturn(restaurants);
        when(restaurantMapper.toRestaurantResponse(r1)).thenReturn(createRestaurantResponse(1L, ""));
        when(restaurantMapper.toRestaurantResponse(r2)).thenReturn(createRestaurantResponse(2L, ""));

        // Thực thi
        List<RestaurantResponse> result = restaurantService.getRestaurants();

        // Kiểm tra
        assertEquals(2, result.size());
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void testGetRestaurants_EmptyList() {
        // Chuẩn bị dữ liệu
        when(restaurantRepository.findAll()).thenReturn(Collections.emptyList());

        // Thực thi
        List<RestaurantResponse> result = restaurantService.getRestaurants();

        // Kiểm tra
        assertTrue(result.isEmpty());
        verify(restaurantRepository, times(1)).findAll();
    }

    // --- Kiểm thử cho updateRestaurant (restaurantId, RestaurantUpdateRequest) ---

    @Test
    void testUpdateRestaurant_WithRestaurantId_Success() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest(2L, 3);
        Restaurant restaurant = createRestaurant(restaurantId);
        Package pack = createPackage(2L);
        restaurant.setRestaurantPackage(pack);
        restaurant.setMonthsRegister(3);
        RestaurantResponse response = createRestaurantResponse(restaurantId, "");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageService.findPackById(2L)).thenReturn(pack);
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantResponse(restaurant)).thenReturn(response);
        doNothing().when(restaurantMapper).updateRestaurant(restaurant, request);

        // Thực thi
        RestaurantResponse result = restaurantService.updateRestaurant(restaurantId, request);

        // Kiểm tra
        assertEquals(restaurantId, result.getId());
        assertEquals(request.getMonths()-1, ChronoUnit.MONTHS.between(LocalDateTime.now(), restaurant.getExpiryDate()));
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    // --- Kiểm thử cho updateRestaurant (accountId, RestaurantManagerUpdateRequest) ---

    @Test
    void testUpdateRestaurant_WithManagerRequest_Success() {
        // Chuẩn bị dữ liệu
        Long accountId = 1L;
        RestaurantManagerUpdateRequest request = RestaurantManagerUpdateRequest.builder()
                .address("address")
                .restaurantName("name")
                .district("district")
                .province("province")
                .build();
        Restaurant restaurant = createRestaurant(1L);
        restaurant.setRestaurantName("address");
        restaurant.setRestaurantName("name");
        restaurant.setDistrict("district");
        restaurant.setProvince("province");
        RestaurantResponse response = createRestaurantResponse(1L, "");

        when(restaurantRepository.findByAccount_Id(accountId)).thenReturn(restaurant);
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantResponse(restaurant)).thenReturn(response);
        doNothing().when(restaurantMapper).updateRestaurant(restaurant, request);

        // Thực thi
        RestaurantResponse result = restaurantService.updateRestaurant(accountId, request);

        // Kiểm tra
        assertEquals(1L, result.getId());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testUpdateRestaurant_WithManagerRequest_NotExist() {
        // Chuẩn bị dữ liệu
        Long accountId = 1L;
        RestaurantManagerUpdateRequest request = new RestaurantManagerUpdateRequest();

        when(restaurantRepository.findByAccount_Id(accountId)).thenReturn(null);

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                restaurantService.updateRestaurant(accountId, request));
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    // --- Kiểm thử cho updateRestaurant (accountId, RestaurantPaymentRequest) ---

    @Test
    void testUpdateRestaurant_WithPaymentRequest_Success() {
        // Chuẩn bị dữ liệu
        Long accountId = 1L;
        RestaurantPaymentRequest request = RestaurantPaymentRequest.builder()
                .ACCOUNT_NAME("account_name")
                .ACCOUNT_NO("01234")
                .BANK_ID("12345")
                .build();
        Restaurant restaurant = createRestaurant(1L);
        restaurant.setACCOUNT_NAME(request.getACCOUNT_NAME());
        restaurant.setBANK_ID(request.getBANK_ID());
        restaurant.setACCOUNT_NAME(request.getACCOUNT_NAME());
        RestaurantResponse response = createRestaurantResponse(1L, "");

        when(restaurantRepository.findByAccount_Id(accountId)).thenReturn(restaurant);
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantResponse(restaurant)).thenReturn(response);
        doNothing().when(restaurantMapper).updateRestaurant(restaurant, request);

        // Thực thi
        RestaurantResponse result = restaurantService.updateRestaurant(accountId, request);

        // Kiểm tra
        assertEquals(1L, result.getId());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testUpdateRestaurant_WithPaymentRequest_NotExist() {
        // Chuẩn bị dữ liệu
        Long accountId = 1L;
        RestaurantPaymentRequest request = new RestaurantPaymentRequest();

        when(restaurantRepository.findByAccount_Id(accountId)).thenReturn(null);

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                restaurantService.updateRestaurant(accountId, request));
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    // --- Kiểm thử cho getRestaurantById ---

    @Test
    void testGetRestaurantById_Success() {
        // Chuẩn bị dữ liệu
        Long id = 1L;
        Restaurant restaurant = createRestaurant(id);

        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));

        // Thực thi
        Restaurant result = restaurantService.getRestaurantById(id);

        // Kiểm tra
        assertEquals(id, result.getId());
    }

    @Test
    void testGetRestaurantById_NotExist() {
        // Chuẩn bị dữ liệu
        Long id = 1L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                restaurantService.getRestaurantById(id));
        assertEquals(ErrorCode.INVALID_KEY, exception.getErrorCode());
    }

    // --- Kiểm thử cho getRestaurantByAccountId ---

    @Test
    void testGetRestaurantByAccountId_Success() {
        // Chuẩn bị dữ liệu
        Long accountId = 1L;
        Restaurant restaurant = createRestaurant(1L);
        restaurant.setVatActive(true);
        RestaurantResponse response = createRestaurantResponse(1L, "");

        when(restaurantRepository.findByAccount_Id(accountId)).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantResponse(restaurant)).thenReturn(response);

        // Thực thi
        RestaurantResponse result = restaurantService.getRestaurantByAccountId(accountId);

        // Kiểm tra
        assertTrue(result.isVatActive());
    }

    @Test
    void testGetRestaurantByAccountId_NotExist() {
        // Chuẩn bị dữ liệu
        Long accountId = 1L;
        when(restaurantRepository.findByAccount_Id(accountId)).thenReturn(null);

        // Thực thi
        RestaurantResponse result = restaurantService.getRestaurantByAccountId(accountId);

        // Kiểm tra
        assertNull(result);
    }

    // --- Kiểm thử cho getMoneyToUpdatePackForRestaurant ---

    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterHigh_MonthsHigh() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest(2L, 12);
        Restaurant restaurant = createRestaurant(restaurantId);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(12);
        Package currentPack = createPackage(1L, 1000.0, 12000.0);
        Package newPack = createPackage(2L, 2000.0, 24000.0);
        restaurant.setRestaurantPackage(currentPack);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageService.findPackById(2L)).thenReturn(newPack);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurantId, request);

        // Kiểm tra (12000 / 365 * 10 ≈ 328.77, 24000 - 328.77 ≈ 23671.23, làm tròn 23671)
        assertEquals(23671, result, 1);
    }

    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterHigh_MonthsLow() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest(2L, 6);
        Restaurant restaurant = createRestaurant(restaurantId);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(12);
        Package currentPack = createPackage(1L, 1000.0, 12000.0);
        Package newPack = createPackage(2L, 2000.0, 24000.0);
        restaurant.setRestaurantPackage(currentPack);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageService.findPackById(2L)).thenReturn(newPack);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurantId, request);

        // Kiểm tra (12000 / 365 * 10 ≈ 328.77, 2000 * 6 - 328.77 ≈ 11671.23, làm tròn 11671)
        assertEquals(11671, result, 1);
    }

    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterLow_MonthsHigh() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest(2L, 12);
        Restaurant restaurant = createRestaurant(restaurantId);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(1);
        Package currentPack = createPackage(1L, 1000.0, 12000.0);
        Package newPack = createPackage(2L, 2000.0, 24000.0);
        restaurant.setRestaurantPackage(currentPack);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageService.findPackById(2L)).thenReturn(newPack);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurantId, request);

        // Kiểm tra (1000 / 30 * 10 ≈ 333.33, 24000 - 333.33 ≈ 23666.67, làm tròn 23667)
        assertEquals(23667, result, 1);
    }

    @Test
    void testGetMoneyToUpdatePack_DayLeftPositive_MonthsRegisterLow_MonthsLow() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest(2L, 6);
        Restaurant restaurant = createRestaurant(restaurantId);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurant.setMonthsRegister(1);
        Package currentPack = createPackage(1L, 1000.0, 12000.0);
        Package newPack = createPackage(2L, 2000.0, 24000.0);
        restaurant.setRestaurantPackage(currentPack);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageService.findPackById(2L)).thenReturn(newPack);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurantId, request);

        // Kiểm tra (1000 / 30 * 10 ≈ 333.33, 2000 * 6 - 333.33 ≈ 11666.67, làm tròn 11667)
        assertEquals(11667, result, 1);
    }

    @Test
    void testGetMoneyToUpdatePack_DayLeftZero_MonthsHigh() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest(2L, 12);
        Restaurant restaurant = createRestaurant(restaurantId);
        restaurant.setExpiryDate(LocalDateTime.now().minusDays(1));
        Package newPack = createPackage(2L, 2000.0, 24000.0);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageService.findPackById(2L)).thenReturn(newPack);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurantId, request);

        // Kiểm tra (24000, làm tròn 24000)
        assertEquals(24000, result, 1);
    }

    @Test
    void testGetMoneyToUpdatePack_DayLeftZero_MonthsLow() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest(2L, 6);
        Restaurant restaurant = createRestaurant(restaurantId);
        restaurant.setExpiryDate(LocalDateTime.now().minusDays(1));
        Package newPack = createPackage(2L, 2000.0, 24000.0);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageService.findPackById(2L)).thenReturn(newPack);

        // Thực thi
        double result = restaurantService.getMoneyToUpdatePackForRestaurant(restaurantId, request);

        // Kiểm tra (2000 * 6 = 12000, làm tròn 12000)
        assertEquals(12000, result, 1);
    }

    // --- Kiểm thử cho updateRestaurantVatById ---

    @Test
    void testUpdateRestaurantVatById_Success() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        Restaurant restaurant = createRestaurant(restaurantId);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        // Thực thi
        restaurantService.updateRestaurantVatById(restaurantId, true);

        // Kiểm tra
        assertTrue(restaurant.isVatActive());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    // --- Kiểm thử cho updatePointForRestaurant ---

    @Test
    void testUpdatePointForRestaurant_Success() {
        // Chuẩn bị dữ liệu
        Long restaurantId = 1L;
        PointsRequest request = createPointsRequest(200000, 2000);
        Restaurant restaurant = createRestaurant(restaurantId);
        RestaurantResponse response = createRestaurantResponse(restaurantId, "");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        when(restaurantMapper.toRestaurantResponse(restaurant)).thenReturn(response);

        // Thực thi
        RestaurantResponse result = restaurantService.updatePointForRestaurant(restaurantId, request);

        // Kiểm tra
        assertEquals(restaurantId, result.getId());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    // --- Kiểm thử cho countRestaurantByDateCreated ---

    @Test
    void testCountRestaurantByDateCreated() {
        // Chuẩn bị dữ liệu
        LocalDate date = LocalDate.now();
        when(restaurantRepository.countByDateCreated(date)).thenReturn(5);

        // Thực thi
        int result = restaurantService.countRestaurantByDateCreated(date);

        // Kiểm tra
        assertEquals(5, result);
        verify(restaurantRepository, times(1)).countByDateCreated(date);
    }

    private RestaurantRequest createRestaurantRequest(Long accountId, String name) {
        return RestaurantRequest.builder()
                .accountId(accountId)
                .restaurantName(name)
                .build();
    }

    private Account createAccount(Long id) {
        return Account.builder()
                .id(id)
                .build();
    }

    private Restaurant createRestaurant(Long id) {
        return Restaurant.builder()
                .id(id)
                .build();
    }

    private RestaurantResponse createRestaurantResponse(Long id, String token) {
        return RestaurantResponse.builder()
                .id(id)
                .token(token)
                .build();
    }

    private Package createPackage(String name) {
        return Package.builder()
                .packName(name)
                .build();
    }

    private Package createPackage(Long id) {
        return Package.builder()
                .id(id)
                .build();
    }


    private Package createPackage(Long id, double pricePerMonth, double pricePerYear) {
        return Package.builder()
                .id(id)
                .pricePerYear(pricePerYear)
                .pricePerMonth(pricePerMonth)
                .build();
    }

    private PointsRequest createPointsRequest(double moneyToPoint, double pointToMoney) {
        return PointsRequest.builder()
                .moneyToPoint(moneyToPoint)
                .pointToMoney(pointToMoney)
                .build();
    }
}
