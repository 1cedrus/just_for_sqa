package com.restaurent.manager.repository;

import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.entity.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest//@ActiveProfiles("test") // Sử dụng profile test để chạy các test case
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DishCategoryRepositoryTest {

    // Inject DishCategoryRepository để test các phương thức của nó
    @Autowired
    private DishCategoryRepository dishCategoryRepository;

    //     Inject TestEntityManager để chèn dữ liệu trực tiếp vào H2 Database
    @Autowired
    private TestEntityManager entityManager;

    // Biến mẫu để tái sử dụng trong các test case
    private DishCategory sampleCategory;
    private Restaurant sampleRestaurant;

    /**
     * Thiết lập dữ liệu trước mỗi test case.
     * Chạy trước mỗi @Test để đảm bảo có dữ liệu mẫu trong H2 Database.
     */
    @BeforeEach
    public void setUp() {
        Restaurant setupRestaurant = new Restaurant();

        DishCategory setupDishCategory = new DishCategory();
        setupDishCategory.setName("Drinks");
        setupDishCategory.setCode("drinks");
        setupDishCategory.setRestaurant(setupRestaurant);

        // Dùng TestEntityManager để chèn dữ liệu vào H2 Database

        sampleRestaurant = entityManager.persist(setupRestaurant);
        sampleCategory = entityManager.persist(setupDishCategory);
        // Đảm bảo dữ liệu được ghi ngay vào database (đồng bộ)
        entityManager.flush();
    }

    /**
     * Test trường hợp existsByNameAndRestaurant_Id trả về true (dữ liệu tồn tại).
     * Kiểm tra nhánh thành công của phương thức.
     */
    // TestcaseID: DCR-1
    @Test
    public void testExistsByNameAndRestaurantId_Exists() {
        // Gọi phương thức để kiểm tra xem dữ liệu đã chèn có tồn tại không
        boolean exists = dishCategoryRepository.existsByNameAndRestaurant_Id("Drinks", sampleRestaurant.getId());
        // Xác nhận kết quả là true (nhánh thành công)
        assertTrue(exists, "Phải tìm thấy category với name 'Drinks' và restaurantId " + sampleRestaurant.getId());
    }

    /**
     * Test trường hợp existsByNameAndRestaurant_Id trả về false (dữ liệu không tồn tại).
     * Kiểm tra nhánh thất bại của phương thức.
     */
    // TestcaseID: DCR-2
    @Test
    public void testExistsByNameAndRestaurantId_NotExists() {
        // Kiểm tra với một name không tồn tại trong database
        boolean exists = dishCategoryRepository.existsByNameAndRestaurant_Id("Food", sampleRestaurant.getId());
        // Xác nhận kết quả là false (nhánh thất bại)
        assertFalse(exists, "Không được tìm thấy category với name 'Food'");
    }

    /**
     * Test phương thức findByRestaurantIdAndNameContaining khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công với phân trang.
     */
    // TestcaseID: DCR-3
    @Test
    public void testFindByRestaurantIdAndNameContaining() {
        // Tạo Pageable để giới hạn kết quả (page 0, size 10)
        Pageable pageable = PageRequest.of(0, 10);
        // Gọi phương thức với query khớp với dữ liệu đã chèn
        List<DishCategory> result = dishCategoryRepository.findByRestaurant_IdAndNameContaining(sampleRestaurant.getId(), "Drink", pageable);
        // Xác nhận tìm thấy 1 kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 category");
        // Xác nhận tên của category tìm thấy là đúng
        assertEquals("Drinks", result.get(0).getName(), "Tên category phải là 'Drinks'");
    }

    /**
     * Test phương thức findByRestaurantIdAndNameContaining khi không tìm thấy dữ liệu.
     * Kiểm tra nhánh thất bại với phân trang.
     */
    // TestcaseID: DCR-4
    @Test
    public void testFindByRestaurantIdAndNameContaining_NoMatch() {
        // Tạo Pageable để giới hạn kết quả
        Pageable pageable = PageRequest.of(0, 10);
        // Gọi phương thức với query không khớp
        List<DishCategory> result = dishCategoryRepository.findByRestaurant_IdAndNameContaining(sampleRestaurant.getId(), "Food", pageable);
        // Xác nhận không tìm thấy kết quả nào
        assertTrue(result.isEmpty(), "Không được tìm thấy category nào với query 'Food'");
    }

    /**
     * Test phương thức countByRestaurantIdAndNameContaining khi đếm được dữ liệu.
     * Kiểm tra nhánh thành công của đếm.
     */
    // TestcaseID: DCR-5
    @Test
    public void testCountByRestaurantIdAndNameContaining() {
        // Gọi phương thức đếm với query khớp dữ liệu
        int count = dishCategoryRepository.countByRestaurant_IdAndNameContaining(sampleRestaurant.getId(), "Drink");
        // Xác nhận đếm được 1 bản ghi
        assertEquals(1, count, "Phải đếm được 1 category với query 'Drink'");
    }

    /**
     * Test phương thức countByRestaurantIdAndNameContaining khi không đếm được dữ liệu.
     * Kiểm tra nhánh thất bại của đếm.
     */
    // TestcaseID: DCR-6
    @Test
    public void testCountByRestaurantIdAndNameContaining_NoMatch() {
        // Gọi phương thức đếm với query không khớp
        int count = dishCategoryRepository.countByRestaurant_IdAndNameContaining(sampleRestaurant.getId(), "Food");
        // Xác nhận đếm được 0 bản ghi
        assertEquals(0, count, "Phải đếm được 0 category với query 'Food'");
    }

    /**
     * Test phương thức findByRestaurantId khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công của tìm danh sách.
     */
    // TestcaseID: DCR-7
    @Test
    public void testFindByRestaurantId() {
        // Gọi phương thức tìm danh sách category theo restaurantId
        List<DishCategory> result = dishCategoryRepository.findByRestaurant_Id(sampleRestaurant.getId());
        // Xác nhận tìm thấy 1 category
        assertEquals(1, result.size(), "Phải tìm thấy 1 category với restaurantId " + sampleRestaurant.getId());
        // Xác nhận tên category là đúng
        assertEquals("Drinks", result.get(0).getName(), "Tên category phải là 'Drinks'");
    }

    /**
     * Test phương thức findByRestaurantId khi không tìm thấy dữ liệu.
     * Kiểm tra nhánh thất bại của tìm danh sách.
     */
    // TestcaseID: DCR-8
    @Test
    public void testFindByRestaurantId_NoMatch() {
        // Gọi phương thức với restaurantId không có dữ liệu
        List<DishCategory> result = dishCategoryRepository.findByRestaurant_Id(2L);
        // Xác nhận không tìm thấy category nào
        assertTrue(result.isEmpty(), "Không được tìm thấy category nào với restaurantId 2");
    }

    /**
     * Test phương thức findByCodeAndRestaurantId khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công (Optional có giá trị).
     */
    // TestcaseID: DCR-9
    @Test
    public void testFindByCodeAndRestaurantId_Found() {
        // Gọi phương thức với code và restaurantId khớp dữ liệu
        Optional<DishCategory> result = dishCategoryRepository.findByCodeAndRestaurant_Id("drinks", sampleRestaurant.getId());
        // Xác nhận tìm thấy category
        assertTrue(result.isPresent(), "Phải tìm thấy category với code 'drinks'");
        // Xác nhận tên category là đúng
        assertEquals("Drinks", result.get().getName(), "Tên category phải là 'Drinks'");
    }

    /**
     * Test phương thức findByCodeAndRestaurantId khi không tìm thấy dữ liệu.
     * Kiểm tra nhánh thất bại (Optional rỗng).
     */
    // TestcaseID: DCR-10
    @Test
    public void testFindByCodeAndRestaurantId_NotFound() {
        // Gọi phương thức với code không khớp
        Optional<DishCategory> result = dishCategoryRepository.findByCodeAndRestaurant_Id("food", sampleRestaurant.getId());
        // Xác nhận không tìm thấy category
        assertFalse(result.isPresent(), "Không được tìm thấy category với code 'food'");
    }
}