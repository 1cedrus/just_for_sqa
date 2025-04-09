package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DishRepositoryTest {

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Dish sampleDish;
    private Restaurant sampleRestaurant;
    private DishCategory sampleCategory;
    private Unit sampleUnit;

    @BeforeEach
    public void setUp() {
        // Tạo dữ liệu mẫu
        Restaurant setupRestaurant = new Restaurant();
        DishCategory setupCategory = new DishCategory();
        Unit setupUnit = new Unit();

        Dish setupDish = new Dish();
        setupDish.setName("Pizza");
        setupDish.setStatus(true);
        setupDish.setRestaurant(setupRestaurant);
        setupDish.setDishCategory(setupCategory);
        setupDish.setUnit(setupUnit);

        // Persist dữ liệu vào H2 database
        sampleRestaurant = entityManager.persist(setupRestaurant);
        sampleCategory = entityManager.persist(setupCategory);
        sampleUnit = entityManager.persist(setupUnit);
        sampleDish = entityManager.persist(setupDish);
        entityManager.flush();
    }

    // Test findByRestaurant_Id - Thành công
    @Test
    public void testFindByRestaurantId_Success() {
        List<Dish> result = dishRepository.findByRestaurant_Id(sampleRestaurant.getId());
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish với restaurantId " + sampleRestaurant.getId());
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    // Test findByRestaurant_Id - Thất bại
    @Test
    public void testFindByRestaurantId_NoMatch() {
        List<Dish> result = dishRepository.findByRestaurant_Id(999L);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish nào với restaurantId 999");
    }

    // Test findByDishCategory_IdAndStatus - Thành công
    @Test
    public void testFindByDishCategoryIdAndStatus_Success() {
        List<Dish> result = dishRepository.findByDishCategory_IdAndStatus(sampleCategory.getId(), true);
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish với categoryId " + sampleCategory.getId() + " và status true");
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    // Test findByDishCategory_IdAndStatus - Thất bại
    @Test
    public void testFindByDishCategoryIdAndStatus_NoMatch() {
        List<Dish> result = dishRepository.findByDishCategory_IdAndStatus(sampleCategory.getId(), false);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish nào với categoryId " + sampleCategory.getId() + " và status false");
    }

    // Test findByRestaurant_IdAndStatusAndNameContaining - Thành công
    @Test
    public void testFindByRestaurantIdAndStatusAndNameContaining_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Dish> result = dishRepository.findByRestaurant_IdAndStatusAndNameContaining(
                sampleRestaurant.getId(), true, pageable, "Piz");
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish với restaurantId " + sampleRestaurant.getId() + " và query 'Piz'");
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    // Test findByRestaurant_IdAndStatusAndNameContaining - Thất bại
    @Test
    public void testFindByRestaurant_IdAndStatusAndNameContaining_NoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Dish> result = dishRepository.findByRestaurant_IdAndStatusAndNameContaining(
                sampleRestaurant.getId(), true, pageable, "Burger");
        assertTrue(result.isEmpty(), "Không được tìm thấy dish nào với query 'Burger'");
    }

    // Test countByRestaurant_IdAndStatusAndNameContaining - Thành công
    @Test
    public void testCountByRestaurantIdAndStatusAndNameContaining_Success() {
        int count = dishRepository.countByRestaurant_IdAndStatusAndNameContaining(
                sampleRestaurant.getId(), true, "Piz");
        assertEquals(1, count, "Phải đếm được 1 dish với restaurantId " + sampleRestaurant.getId() + " và query 'Piz'");
    }

    // Test countByRestaurant_IdAndStatusAndNameContaining - Thất bại
    @Test
    public void testCountByRestaurantIdAndStatusAndNameContaining_NoMatch() {
        int count = dishRepository.countByRestaurant_IdAndStatusAndNameContaining(
                sampleRestaurant.getId(), true, "Burger");
        assertEquals(0, count, "Phải đếm được 0 dish với query 'Burger'");
    }

    // Test existsByUnit_Id - Thành công
    @Test
    public void testExistsByUnitId_Success() {
        boolean exists = dishRepository.existsByUnit_Id(sampleUnit.getId());
        assertTrue(exists, "Phải tồn tại dish với unitId " + sampleUnit.getId());
    }

    // Test existsByUnit_Id - Thất bại
    @Test
    public void testExistsByUnitId_NoMatch() {
        boolean exists = dishRepository.existsByUnit_Id(999L);
        assertFalse(exists, "Không được tồn tại dish với unitId 999");
    }

    // Test findByRestaurant_IdAndStatus - Thành công
    @Test
    public void testFindByRestaurantIdAndStatus_Success() {
        List<Dish> result = dishRepository.findByRestaurant_IdAndStatus(sampleRestaurant.getId(), true);
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish với restaurantId " + sampleRestaurant.getId() + " và status true");
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    // Test findByRestaurant_IdAndStatus - Thất bại
    @Test
    public void testFindByRestaurantIdAndStatus_NoMatch() {
        List<Dish> result = dishRepository.findByRestaurant_IdAndStatus(sampleRestaurant.getId(), false);
        assertTrue(result.isEmpty(), "Không được tìm thấy dish nào với restaurantId " + sampleRestaurant.getId() + " và status false");
    }
}
