package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.RestaurantPackagePaymentHistoryRequest;
import com.restaurent.manager.dto.request.restaurant.RestaurantUpdateRequest;
import com.restaurent.manager.dto.response.StatisticAdminTable;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.RestaurantPackagePaymentHistory;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.entity.Permission;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.RestaurantPackageHistoryMapper;
import com.restaurent.manager.repository.RestaurantPackagePaymentHistoryRepository;
import com.restaurent.manager.repository.PackageRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.PermissionRepository;
import com.restaurent.manager.service.impl.AccountService;
import com.restaurent.manager.service.impl.RestaurantPackagePaymentHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho RestaurantPackagePaymentHistoryService
 * Sử dụng database thật với profile test
 * Mục tiêu: Đạt branch coverage khoảng 80% cho tất cả các phương thức
 * Các test tập trung vào kiểm tra logic chính và các nhánh quan trọng
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RestaurantPackagePaymentHistoryServiceTest {

    @Autowired
    private RestaurantPackagePaymentHistoryService service; // Service cần test

    @Autowired
    private RestaurantPackagePaymentHistoryRepository restaurantPackagePaymentHistoryRepository; // Repository để lưu trữ lịch sử thanh toán

    @Autowired
    private IPackageService packageService; // Service để tìm package

    @Autowired
    private PackageRepository packageRepository; // Repository để quản lý package

    @Autowired
    private IRestaurantService restaurantService; // Service để quản lý restaurant

    @Autowired
    private RestaurantRepository restaurantRepository; // Repository để quản lý restaurant

    @Autowired
    private AccountRepository accountRepository; // Repository để quản lý account

    @Autowired
    private PermissionRepository permissionRepository; // Repository để quản lý permission

    @Autowired
    private AccountService accountService; // Service để quản lý account

    @Autowired
    private IEmailService emailService; // Service để gửi email

    /**
     * Thiết lập trước mỗi test case
     * Clean database để đảm bảo môi trường test sạch sẽ
     */
    @BeforeEach
    void setUp() {
        // Clean up database before each test
        restaurantPackagePaymentHistoryRepository.deleteAll();
        restaurantRepository.deleteAll();
        packageRepository.deleteAll();
        accountRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    // --- Tests cho createRestaurantPackagePaymentHistory ---
    /**
     * ID: RPPHS-2
     * Test tạo một RestaurantPackagePaymentHistory thành công khi đã có history tồn tại
     * Kiểm tra xem phương thức có tạo và lưu lịch sử thanh toán đúng với request không
     */
    @Test
    void testCreateRestaurantPackagePaymentHistory_SuccessWithExistHistory() {
        // Chuẩn bị dữ liệu test
        // Tạo account
        Account account = createAccount("test@example.com");
        account = accountRepository.save(account);

        // Tạo package
        Package pack = createPackage("BASIC", 100.0);
        pack = packageRepository.save(pack);

        // Tạo restaurant
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        // Tạo một history đã tồn tại
        RestaurantPackagePaymentHistory existedHistory = createRestaurantPackagePaymentHistory(pack.getId(), restaurant.getId(), 100.0, 12);
        existedHistory.setId(1L); // Set ID manually
        existedHistory.setDateCreated(LocalDateTime.now());
        restaurantPackagePaymentHistoryRepository.save(existedHistory);

        // Tạo request mới
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(
                account.getId(), pack.getId(), restaurant.getId(), 200.0, 6);

        // Thực thi phương thức cần test
        Long result = service.createRestaurantPackagePaymentHistory(request);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertTrue(result > 1L); // ID mới phải lớn hơn 1 vì đã có history tồn tại

        // Verify in database
        Optional<RestaurantPackagePaymentHistory> savedHistory = restaurantPackagePaymentHistoryRepository.findById(result);
        assertTrue(savedHistory.isPresent());
        assertEquals(pack.getId(), savedHistory.get().getPackageId());
        assertEquals(restaurant.getId(), savedHistory.get().getRestaurantId());
        assertEquals(200.0, savedHistory.get().getTotalMoney());
        assertEquals(6, savedHistory.get().getMonths());
    }

    /**
     * ID: RPPHS-1
     * Test tạo một RestaurantPackagePaymentHistory thành công khi chưa có history nào
     * Kiểm tra xem phương thức có tạo và lưu lịch sử thanh toán đúng với request không
     */
    @Test
    void testCreateRestaurantPackagePaymentHistory_Success() {
        // Chuẩn bị dữ liệu test
        // Tạo account
        Account account = createAccount("test@example.com");
        account = accountRepository.save(account);

        // Tạo package
        Package pack = createPackage("BASIC", 100.0);
        pack = packageRepository.save(pack);

        // Tạo restaurant
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        // Tạo request
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(
                account.getId(), pack.getId(), restaurant.getId(), 100.0, 12);

        // Thực thi phương thức cần test
        Long result = service.createRestaurantPackagePaymentHistory(request);

        // Kiểm tra kết quả
        assertEquals(1L, result); // ID đầu tiên phải là 1

        // Verify in database
        Optional<RestaurantPackagePaymentHistory> savedHistory = restaurantPackagePaymentHistoryRepository.findById(result);
        assertTrue(savedHistory.isPresent());
        assertEquals(pack.getId(), savedHistory.get().getPackageId());
        assertEquals(restaurant.getId(), savedHistory.get().getRestaurantId());
        assertEquals(100.0, savedHistory.get().getTotalMoney());
        assertEquals(12, savedHistory.get().getMonths());
    }

    // --- Tests cho getNewId ---
    /**
     * ID: RPPHS-3
     * Test lấy ID mới khi danh sách lịch sử rỗng
     * Kiểm tra xem ID trả về có phải là 1 khi không có bản ghi nào không
     */
    @Test
    void testGetNewId_EmptyList() {
        // Thực thi phương thức
        Long result = service.getNewId();

        // Kiểm tra kết quả
        assertEquals(1L, result); // Khi danh sách rỗng, ID mới phải là 1
    }

    /**
     * ID: RPPHS-4
     * Test lấy ID mới khi đã có dữ liệu
     * Kiểm tra xem ID mới có được tăng lên từ ID cuối cùng không
     */
    @Test
    void testGetNewId_WithData() {
        // Chuẩn bị dữ liệu: tạo một history với ID tự động
        Restaurant restaurant = Restaurant.builder().restaurantName("restaurant").build();
        restaurantRepository.saveAndFlush(restaurant);

        RestaurantPackagePaymentHistory history = createRestaurantPackagePaymentHistory(1L, restaurant.getId(), 100.0, 12);
        history.setId(5L); // Set ID manually
        history.setDateCreated(LocalDateTime.now());
        RestaurantPackagePaymentHistory savedHistory = restaurantPackagePaymentHistoryRepository.save(history);

        // Thực thi
        Long result = service.getNewId();

        // Kiểm tra
        assertEquals(savedHistory.getId() + 1, result); // ID mới phải là ID cuối + 1
    }

    // --- Tests cho updateRestaurantPackagePaymentHistory ---
    /**
     * ID: RPPHS-5
     * Test cập nhật lịch sử thanh toán thành công
     * Kiểm tra xem trạng thái paid được cập nhật và email được gửi không
     */
    @Test
    void testUpdateRestaurantPackagePaymentHistory_Success() {
        // Chuẩn bị dữ liệu
        // Tạo account
        Account account = createAccount("test@example.com");
        account = accountRepository.save(account);

        // Tạo package
        Package pack = createPackage("BASIC", 100.0);
        pack = packageRepository.save(pack);

        // Tạo restaurant
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        // Tạo history
        RestaurantPackagePaymentHistory history = createRestaurantPackagePaymentHistory(pack.getId(), restaurant.getId(), 100.0, 12);
        history.setId(1L); // Set ID manually
        history.setDateCreated(LocalDateTime.now());
        history = restaurantPackagePaymentHistoryRepository.save(history);

        // Tạo request cập nhật
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(
                account.getId(), pack.getId(), restaurant.getId(), 100.0, 12);

        // Thực thi
        String result = service.updateRestaurantPackagePaymentHistory(history.getId(), request);

        // Kiểm tra
        assertNotNull(result); // Token không được null

        // Verify in database - kiểm tra history đã được cập nhật
        Optional<RestaurantPackagePaymentHistory> updatedHistory = restaurantPackagePaymentHistoryRepository.findById(history.getId());
        assertTrue(updatedHistory.isPresent());
        assertTrue(updatedHistory.get().isPaid()); // Trạng thái paid phải là true
    }

    /**
     * ID: RPPHS-6
     * Test cập nhật với ID không tồn tại
     * Kiểm tra xem exception có được ném ra khi không tìm thấy history không
     */
    @Test
    void testUpdateRestaurantPackagePaymentHistory_NotFound() {
        // Chuẩn bị dữ liệu
        Long nonExistentId = 999L;
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(1L, 1L, 1L, 100.0, 12);

        // Thực thi và mong đợi exception
        assertThrows(AppException.class, () -> 
            service.updateRestaurantPackagePaymentHistory(nonExistentId, request));
    }

    // --- Tests cho getTotalValueByDate ---
    /**
     * ID: RPPHS-7
     * Test lấy tổng giá trị với code hợp lệ
     * Kiểm tra xem phương thức có trả về danh sách khi code đúng không
     */
    @Test
    void testGetTotalValueByDate_ValidCode() {
        // Chuẩn bị dữ liệu
        String code = "current-week";

        // Thực thi
        List<StatisticAdminTable> result = service.getTotalValueByDate(code);

        // Kiểm tra
        assertNotNull(result); // Kết quả không được null khi code hợp lệ
        assertFalse(result.isEmpty()); // Danh sách phải có dữ liệu (tuần có 7 ngày)
    }

    /**
     * ID: RPPHS-8
     * Test lấy tổng giá trị với code không hợp lệ
     * Kiểm tra xem phương thức trả về null khi code không khớp
     */
    @Test
    void testGetTotalValueByDate_InvalidCode() {
        // Chuẩn bị dữ liệu
        String code = "invalid";

        // Thực thi
        List<StatisticAdminTable> result = service.getTotalValueByDate(code);

        // Kiểm tra
        assertNull(result); // Mong đợi null khi code không hợp lệ
    }

    // --- Tests cho getProfitInCurrentMonth ---
    /**
     * ID: RPPHS-9
     * Test lấy lợi nhuận tháng hiện tại
     * Kiểm tra xem danh sách trả về có đúng số ngày từ đầu tháng đến hiện tại không
     */
    @Test
    void testGetProfitInCurrentMonth() {
        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInCurrentMonth();

        // Kiểm tra
        int currentDay = LocalDateTime.now().getDayOfMonth();
        assertEquals(currentDay, result.size()); // Số ngày từ đầu tháng đến hiện tại
        assertNotNull(result.getFirst()); // Phần tử đầu tiên không được null
    }

    // --- Tests cho getProfitInLastMonth ---
    /**
     * ID: RPPHS-10
     * Test lấy lợi nhuận tháng trước
     * Kiểm tra xem danh sách trả về có đúng số ngày của tháng trước không
     */
    @Test
    void testGetProfitInLastMonth() {
        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInLastMonth();

        // Kiểm tra
        int daysInLastMonth = LocalDate.now().minusMonths(1).lengthOfMonth();
        assertEquals(daysInLastMonth, result.size()); // Số ngày trong tháng trước
        assertNotNull(result.getFirst()); // Phần tử đầu tiên không được null
    }

    // --- Tests cho getProfitInCurrentWeek ---
    /**
     * ID: RPPHS-11
     * Test lấy lợi nhuận tuần hiện tại
     * Kiểm tra xem danh sách trả về có đúng 7 ngày của tuần hiện tại không
     */
    @Test
    void testGetProfitInCurrentWeek() {
        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInCurrentWeek();

        // Kiểm tra
        assertEquals(7, result.size()); // Tuần có 7 ngày
        assertNotNull(result.getFirst()); // Phần tử đầu tiên không được null
    }

    // --- Tests cho getProfitInLastWeek ---
    /**
     * ID: RPPHS-12
     * Test lấy lợi nhuận tuần trước
     * Kiểm tra xem danh sách trả về có đúng 7 ngày của tuần trước không
     */
    @Test
    void testGetProfitInLastWeek() {
        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInLastWeek();

        // Kiểm tra
        assertEquals(7, result.size()); // Tuần có 7 ngày
        assertNotNull(result.getFirst()); // Phần tử đầu tiên không được null
    }

    // --- Tests cho totalValueInDate ---
    /**
     * ID: RPPHS-13
     * Test tính tổng giá trị trong ngày - có dữ liệu
     * Kiểm tra xem tổng chỉ tính các history đã paid không
     */
    @Test
    void testTotalValueInDate_WithData() {
        // Chuẩn bị dữ liệu
        LocalDate date = LocalDate.now();
        LocalDateTime dateTime = date.atStartOfDay();
        
        // Tạo history đã thanh toán
        RestaurantPackagePaymentHistory paidHistory = createRestaurantPackagePaymentHistory(1L, 1L, 1000.0, 12);
        paidHistory.setId(1L); // Set ID manually
        paidHistory.setPaid(true);
        paidHistory.setDateCreated(dateTime); // Use LocalDateTime
        restaurantPackagePaymentHistoryRepository.save(paidHistory);

        // Tạo history chưa thanh toán
        RestaurantPackagePaymentHistory unpaidHistory = createRestaurantPackagePaymentHistory(1L, 2L, 500.0, 6);
        unpaidHistory.setId(2L); // Set ID manually
        unpaidHistory.setPaid(false);
        unpaidHistory.setDateCreated(dateTime); // Use LocalDateTime
        restaurantPackagePaymentHistoryRepository.save(unpaidHistory);

        // Thực thi
        double result = service.totalValueInDate(date);

        // Kiểm tra
        assertEquals(1000.0, result); // Chỉ tính history đã paid (1000.0)
    }

    /**
     * ID: RPPHS-14
     * Test tính tổng giá trị trong ngày - không có dữ liệu
     * Kiểm tra xem trả về 0 khi không có history nào
     */
    @Test
    void testTotalValueInDate_NoData() {
        // Chuẩn bị dữ liệu
        LocalDate date = LocalDate.now();

        // Thực thi
        double result = service.totalValueInDate(date);

        // Kiểm tra
        assertEquals(0.0, result); // Không có dữ liệu -> trả về 0
    }

    // Các phương thức helper để tạo đối tượng test
    private RestaurantPackagePaymentHistoryRequest createRestaurantPackagePaymentHistoryRequest(
            Long accountId,
            Long packageId,
            Long restaurantId,
            double totalMoney,
            int month
    ) {
        RestaurantPackagePaymentHistoryRequest request = new RestaurantPackagePaymentHistoryRequest();
        request.setAccountId(accountId);
        request.setPackageId(packageId);
        request.setRestaurantId(restaurantId);
        request.setTotalMoney(totalMoney);
        request.setMonths(month);
        return request;
    }

    private RestaurantPackagePaymentHistory createRestaurantPackagePaymentHistory(
            Long packageId,
            Long restaurantId,
            double totalMoney,
            int month
    ) {
       RestaurantPackagePaymentHistory history = new RestaurantPackagePaymentHistory();
       history.setPackageId(packageId);
       history.setRestaurantId(restaurantId);
       history.setMonths(month);
       history.setTotalMoney(totalMoney);
       history.setPaid(false); // Default
       return history;
    }

    private Package createPackage(String name, double price) {
        Package pack = new Package();
        pack.setPackName(name);
        pack.setPricePerMonth(price);
        pack.setPermissions(new HashSet<>());
        return pack;
    }

    private Account createAccount(String email) {
        Account account = new Account();
        account.setEmail(email);
        account.setUsername("testuser");
        account.setPassword("password");
        return account;
    }

    private Restaurant createRestaurant(String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName(name);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(30));
        return restaurant;
    }
}