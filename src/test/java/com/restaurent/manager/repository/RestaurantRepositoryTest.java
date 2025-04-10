package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
/**
 * Unit test class cho RestaurantRepository
 * Mục tiêu: Đạt branch coverage khoảng 80% trở lên
 * Sử dụng Mockito để mock repository và JUnit 5 để test
 */
public class RestaurantRepositoryTest {
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private AccountRepository accountRepository;

    /**
     * Thiết lập trước mỗi test case
     * Khởi tạo mock cho repository
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
    }

    // --- Tests cho findByRestaurantPackageIdIsNotNullAndExpiryDateBefore ---
    /**
     * Test tìm restaurant với package không null và đã hết hạn - có kết quả
     */
    @Test
    void testChuan1_FindByRestaurantPackageIdIsNotNullAndExpiryDateBefore() {
        LocalDateTime now = LocalDateTime.now(); // Thời điểm hiện tại

        // Tạo dữ liệu test: restaurant với package không null và đã hết hạn
        Package pkg = Package.builder().build();
        packageRepository.saveAndFlush(pkg);

        Restaurant restaurant = Restaurant.builder()
                .restaurantName("Restaurant")
                .restaurantPackage(pkg)
                .expiryDate(now.minusDays(1))
                .build();
        restaurantRepository.saveAndFlush(restaurant);

        // Thực thi phương thức cần test
        List<Restaurant> result = restaurantRepository
                .findByRestaurantPackageIdIsNotNullAndExpiryDateBefore(now);

        // Kiểm tra kết quả
        assertNotNull(result);           // Kết quả không được null
        assertEquals(1, result.size());  // Phải có 1 phần tử
        assertEquals(restaurant.getRestaurantName(), result.getFirst().getRestaurantName()); // Phải khớp với restaurant khởi tạo
        assertEquals(restaurant.getRestaurantPackage(), result.getFirst().getRestaurantPackage()); // Gói phải khớp với dữ liệu khởi tạo
        assertEquals(restaurant.getExpiryDate(), result.getFirst().getExpiryDate()); // Thời gian hết hạn khớp với dữ liệu khởi tạo
    }

    /**
     * Test tìm restaurant với package không null và đã hết hạn - không có kết quả
     */
    @Test
    void testNgoaiLe1_FindByRestaurantPackageIdIsNotNullAndExpiryDateBefore() {
        LocalDateTime now = LocalDateTime.now();

        // Thực thi phương thức
        List<Restaurant> result = restaurantRepository
                .findByRestaurantPackageIdIsNotNullAndExpiryDateBefore(now);

        // Kiểm tra kết quả
        assertNotNull(result);     // Kết quả không được null
        assertTrue(result.isEmpty()); // Danh sách phải rỗng
    }

    // --- Tests cho findByAccount_Id ---
    /**
     * Test tìm restaurant theo account ID - tìm thấy
     */
    @Test
    void testChuan1_FindByAccount_Id() {
        // Tạo restaurant, account test
        Account account = Account.builder().build();
        Restaurant restaurant = Restaurant.builder()
                .account(account)
                .build();
        accountRepository.saveAndFlush(account);
        restaurantRepository.saveAndFlush(restaurant);

        // Thực thi
        Restaurant result = restaurantRepository.findByAccount_Id(account.getId());

        // Kiểm tra
        assertNotNull(result);         // Không được null
        assertEquals(restaurant, result); // Phải khớp với du lieu test
    }

    /**
     * Test tìm restaurant theo account ID - không tìm thấy
     */
    @Test
    void testFindByAccount_Id_NotFound() {
        Long accountId = 1L;

        // thực thi phương thức
        Restaurant result = restaurantRepository.findByAccount_Id(accountId);

        assertNull(result); // Phải là null
    }

    // --- Tests cho existsByRestaurantName ---
    /**
     * Test kiểm tra tồn tại restaurant theo tên - tồn tại
     */
    @Test
    void testChuan1_ExistsByRestaurantName() {
        String name = "Test Restaurant";

        // tạo dữ liệu restaurant test
        Restaurant restaurant = Restaurant.builder().restaurantName(name).build();
        restaurantRepository.saveAndFlush(restaurant);

        // thực thi phương thức
        boolean result = restaurantRepository.existsByRestaurantName(name);

        assertTrue(result); // Phải trả về true
    }

    /**
     * Test kiểm tra tồn tại restaurant theo tên - không tồn tại
     */
    @Test
    void testNgoaiLe1_ExistsByRestaurantName() {
        String name = "Test Restaurant";

        // thực thi phương thức
        boolean result = restaurantRepository.existsByRestaurantName(name);

        assertFalse(result); // Phải trả về false
    }

    /**
     * Test kiểm tra tồn tại với tên null
     */
    @Test
    void testNgoaiLe2_ExistsByRestaurantName() {
        // thực thi phương thức
        boolean result = restaurantRepository.existsByRestaurantName(null);

        assertFalse(result); // Phải trả về false khi tên null
    }

    // --- Tests cho existsByAccount_Id ---
    /**
     * Test kiểm tra tồn tại theo account ID - tồn tại
     */
    @Test
    void testChuan1_ExistsByAccount_Id() {
        // tạo dữ liệu restaurant, account test
        Account account = Account.builder().build();
        accountRepository.saveAndFlush(account);
        Restaurant restaurant = Restaurant.builder().account(account).build();
        restaurantRepository.saveAndFlush(restaurant);

        // thực thi phương thức
        boolean result = restaurantRepository.existsByAccount_Id(account.getId());

        assertTrue(result); // Phải trả về true
    }

    /**
     * Test kiểm tra tồn tại theo account ID - không tồn tại
     */
    @Test
    void testNgoaiLe_ExistsByAccount_Id() {
        Long accountId = 1L;

        // thực thi phương thức
        boolean result = restaurantRepository.existsByAccount_Id(accountId);

        assertFalse(result); // Phải trả về false
    }

    // --- Tests cho countByRestaurantPackage_Id ---
    /**
     * Test đếm restaurant theo package ID - có kết quả
     */
    @Test
    void testCountByRestaurantPackage_Id_WithResults() {
        // tạo dữ liệu restaurant, package test
        Package pkg = Package.builder().build();
        packageRepository.saveAndFlush(pkg);
        Restaurant restaurant1 = Restaurant.builder().restaurantPackage(pkg).build();
        Restaurant restaurant2 = Restaurant.builder().restaurantPackage(pkg).build();
        restaurantRepository.saveAllAndFlush(List.of(restaurant1, restaurant2));

        // thực thi phương thức
        int result = restaurantRepository.countByRestaurantPackage_Id(pkg.getId());

        assertEquals(2, result); // Phải trả về 2
    }

    /**
     * Test đếm restaurant theo package ID - không có kết quả
     */
    @Test
    void testNgoaiLe1_CountByRestaurantPackage_Id() {
        // tạo dữ liệu restaurant test
        Restaurant restaurant1 = Restaurant.builder().build();
        Restaurant restaurant2 = Restaurant.builder().build();
        restaurantRepository.saveAllAndFlush(List.of(restaurant1, restaurant2));

        // thực thi phương thức
        int result = restaurantRepository.countByRestaurantPackage_Id(1L);

        assertEquals(0, result); // Phải trả về 0
    }

    // --- Tests cho countByDateCreated ---
    /**
     * Test đếm restaurant theo ngày tạo - có kết quả
     */
    @Test
    void testChuan1_CountByDateCreated() {
        LocalDate date = LocalDate.now();
        // tạo dữ liệu restaurant test
        Restaurant restaurant1 = Restaurant.builder().dateCreated(date).build();
        Restaurant restaurant2 = Restaurant.builder().dateCreated(date.minusDays(1L)).build();
        restaurantRepository.saveAllAndFlush(List.of(restaurant1, restaurant2));

        // thực thi phương thức
        int result = restaurantRepository.countByDateCreated(date);

        assertEquals(1, result);
    }

    /**
     * Test đếm restaurant theo ngày tạo - không có kết quả
     */
    @Test
    void testNgoaiLe1_CountByDateCreated() {
        LocalDate date = LocalDate.now();
        // tạo dữ liệu restaurant test
        Restaurant restaurant1 = Restaurant.builder().dateCreated(date.minusDays(1L)).build();
        restaurantRepository.saveAllAndFlush(List.of(restaurant1));

        // thực thi phương thức
        int result = restaurantRepository.countByDateCreated(date);

        assertEquals(0, result);
    }

    /**
     * Test đếm restaurant với ngày tạo null
     */
    @Test
    void testCountByDateCreated_NullDate() {
        LocalDate date = LocalDate.now();
        // tạo dữ liệu restaurant test
        Restaurant restaurant1 = Restaurant.builder().dateCreated(date).build();
        restaurantRepository.saveAllAndFlush(List.of(restaurant1));

        // thực thi phương thức
        int result = restaurantRepository.countByDateCreated(null);

        assertEquals(0, result);
    }
}
