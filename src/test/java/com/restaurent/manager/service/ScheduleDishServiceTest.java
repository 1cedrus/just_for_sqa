package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.response.ScheduleDishResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.*;
import com.restaurent.manager.service.impl.ScheduleDishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho ScheduleDishService
 * Sử dụng database thật với profile test
 * Mục tiêu: Đạt branch coverage khoảng 80% cho tất cả các phương thức
 * Các test tập trung vào kiểm tra logic chính và các nhánh quan trọng
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ScheduleDishServiceTest {

    @Autowired
    private ScheduleDishService service;

    @Autowired
    private IDishService dishService;

    @Autowired
    private IComboService comboService;

    @Autowired
    private ScheduleDishRepository repository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DishCategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        repository.deleteAll();
        scheduleRepository.deleteAll();
        dishRepository.deleteAll();
        comboRepository.deleteAll();
        categoryRepository.deleteAll();
        restaurantRepository.deleteAll();
    }

    // --- Kiểm thử cho createScheduleDish ---
    // ID: SDS-1
    // Kiểm tra tạo với Dish (dishId không null)
    @Test
    void testCreateScheduleDish_WithDish() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        DishCategory category = createCategory("Test Category");
        category = categoryRepository.save(category);

        Dish dish = createDish("Test Dish", restaurant, category);
        dish = dishRepository.save(dish);

        Schedule schedule = createSchedule(restaurant);
        schedule = scheduleRepository.save(schedule);

        DishOrderRequest request = createDishOrderRequest(dish.getId(), null, 2);

        // Thực thi
        service.createScheduleDish(schedule, request);

        // Kiểm tra
        List<ScheduleDish> savedScheduleDishes = repository.findBySchedule_Id(schedule.getId());
        assertEquals(1, savedScheduleDishes.size());
        assertEquals(dish.getId(), savedScheduleDishes.get(0).getDish().getId());
        assertEquals(2, savedScheduleDishes.get(0).getQuantity());
        assertNull(savedScheduleDishes.get(0).getCombo());
    }

    // ID: SDS-2
    // Kiểm tra tạo với Combo (dishId null)
    @Test
    void testCreateScheduleDish_WithCombo() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        Combo combo = createCombo("Test Combo", restaurant);
        combo = comboRepository.save(combo);

        Schedule schedule = createSchedule(restaurant);
        schedule = scheduleRepository.save(schedule);

        DishOrderRequest request = createDishOrderRequest(null, combo.getId(), 3);

        // Thực thi
        service.createScheduleDish(schedule, request);

        // Kiểm tra
        List<ScheduleDish> savedScheduleDishes = repository.findBySchedule_Id(schedule.getId());
        assertEquals(1, savedScheduleDishes.size());
        assertEquals(combo.getId(), savedScheduleDishes.get(0).getCombo().getId());
        assertEquals(3, savedScheduleDishes.get(0).getQuantity());
        assertNull(savedScheduleDishes.get(0).getDish());
    }

    // ID: SDS-3
    // Kiểm tra tạo với Combo null và dish null
    @Test
    void testCreateScheduleDish_WithNullComboAndDish() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        Schedule schedule = createSchedule(restaurant);
        final Schedule savedSchedule = scheduleRepository.save(schedule);

        DishOrderRequest request = createDishOrderRequest(null, null, 3);

        // Thực thi và kiểm tra
        AppException appException = assertThrows(AppException.class, () ->
                service.createScheduleDish(savedSchedule, request)
        );
        
        assertEquals(ErrorCode.SCHEDULE_DISH_REQUEST_INVALID, appException.getErrorCode());
        
        // Verify không có gì được lưu
        List<ScheduleDish> savedScheduleDishes = repository.findBySchedule_Id(schedule.getId());
        assertTrue(savedScheduleDishes.isEmpty());
    }

    // --- Kiểm thử cho findDishOrComboBySchedule ---

    // ID: SDS-4
    // Kiểm tra khi danh sách không rỗng
    @Test
    void testFindDishOrComboBySchedule_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        DishCategory category = createCategory("Test Category");
        category = categoryRepository.save(category);

        Dish dish = createDish("Test Dish", restaurant, category);
        dish = dishRepository.save(dish);

        Combo combo = createCombo("Test Combo", restaurant);
        combo = comboRepository.save(combo);

        Schedule schedule = createSchedule(restaurant);
        schedule = scheduleRepository.save(schedule);

        // Tạo ScheduleDish với dish
        ScheduleDish scheduleDish1 = createScheduleDish(schedule, dish, null, 2);
        repository.save(scheduleDish1);

        // Tạo ScheduleDish với combo
        ScheduleDish scheduleDish2 = createScheduleDish(schedule, null, combo, 3);
        repository.save(scheduleDish2);

        // Thực thi
        List<ScheduleDishResponse> result = service.findDishOrComboBySchedule(schedule.getId());

        // Kiểm tra
        assertEquals(2, result.size());
        
        // Verify có dish và combo
        boolean hasDish = result.stream().anyMatch(r -> r.getDish() != null);
        boolean hasCombo = result.stream().anyMatch(r -> r.getCombo() != null);
        assertTrue(hasDish);
        assertTrue(hasCombo);
    }

    // ID: SDS-5
    // Kiểm tra khi danh sách rỗng
    @Test
    void testFindDishOrComboBySchedule_EmptyList() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        Schedule schedule = createSchedule(restaurant);
        schedule = scheduleRepository.save(schedule);

        // Thực thi (không tạo ScheduleDish nào)
        List<ScheduleDishResponse> result = service.findDishOrComboBySchedule(schedule.getId());

        // Kiểm tra
        assertTrue(result.isEmpty());
    }

    // --- Kiểm thử cho deleteScheduleDishById ---

    // ID: SDS-6
    // Kiểm tra xóa ScheduleDish
    @Test
    void testDeleteScheduleDishById() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        DishCategory category = createCategory("Test Category");
        category = categoryRepository.save(category);

        Dish dish = createDish("Test Dish", restaurant, category);
        dish = dishRepository.save(dish);

        Schedule schedule = createSchedule(restaurant);
        schedule = scheduleRepository.save(schedule);

        ScheduleDish scheduleDish = createScheduleDish(schedule, dish, null, 2);
        scheduleDish = repository.save(scheduleDish);

        // Verify trước khi xóa
        assertTrue(repository.existsById(scheduleDish.getId()));

        // Thực thi
        service.deleteScheduleDishById(scheduleDish.getId());

        // Kiểm tra
        assertFalse(repository.existsById(scheduleDish.getId()));
    }

    // --- Kiểm thử cho findByScheduleId ---

    // ID: SDS-7
    // Kiểm tra khi danh sách không rỗng
    @Test
    void testFindByScheduleId_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        DishCategory category = createCategory("Test Category");
        category = categoryRepository.save(category);

        Dish dish1 = createDish("Test Dish 1", restaurant, category);
        dish1 = dishRepository.save(dish1);

        Dish dish2 = createDish("Test Dish 2", restaurant, category);
        dish2 = dishRepository.save(dish2);

        Schedule schedule = createSchedule(restaurant);
        schedule = scheduleRepository.save(schedule);

        ScheduleDish scheduleDish1 = createScheduleDish(schedule, dish1, null, 2);
        repository.save(scheduleDish1);

        ScheduleDish scheduleDish2 = createScheduleDish(schedule, dish2, null, 3);
        repository.save(scheduleDish2);

        // Thực thi
        List<ScheduleDish> result = service.findByScheduleId(schedule.getId());

        // Kiểm tra
        assertEquals(2, result.size());
        Dish finalDish = dish1;
        assertTrue(result.stream().anyMatch(sd -> sd.getDish().getId().equals(finalDish.getId())));
        Dish finalDish1 = dish2;
        assertTrue(result.stream().anyMatch(sd -> sd.getDish().getId().equals(finalDish1.getId())));
    }

    // ID: SDS-8
    // Kiểm tra khi danh sách rỗng
    @Test
    void testFindByScheduleId_EmptyList() {
        // Chuẩn bị dữ liệu
        Restaurant restaurant = createRestaurant("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        Schedule schedule = createSchedule(restaurant);
        schedule = scheduleRepository.save(schedule);

        // Thực thi (không tạo ScheduleDish nào)
        List<ScheduleDish> result = service.findByScheduleId(schedule.getId());

        // Kiểm tra
        assertTrue(result.isEmpty());
    }

    // Helper methods để tạo đối tượng test
    private Restaurant createRestaurant(String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName(name);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(30));
        return restaurant;
    }

    private DishCategory createCategory(String name) {
        DishCategory category = new DishCategory();
        category.setName(name);
        return category;
    }

    private Dish createDish(String name, Restaurant restaurant, DishCategory category) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setPrice(100.0);
        dish.setRestaurant(restaurant);
        dish.setDishCategory(category);
        return dish;
    }

    private Combo createCombo(String name, Restaurant restaurant) {
        Combo combo = new Combo();
        combo.setName(name);
        combo.setPrice(200.0);
        combo.setRestaurant(restaurant);
        return combo;
    }

    private Schedule createSchedule(Restaurant restaurant) {
        Schedule schedule = new Schedule();
        schedule.setRestaurant(restaurant);
        schedule.setBookedDate(LocalDate.now());
        return schedule;
    }

    private ScheduleDish createScheduleDish(Schedule schedule, Dish dish, Combo combo, int quantity) {
        ScheduleDish scheduleDish = new ScheduleDish();
        scheduleDish.setSchedule(schedule);
        scheduleDish.setDish(dish);
        scheduleDish.setCombo(combo);
        scheduleDish.setQuantity(quantity);
        return scheduleDish;
    }

    private DishOrderRequest createDishOrderRequest(Long dishId, Long comboId, int quantity) {
        return DishOrderRequest.builder()
                .dishId(dishId)
                .comboId(comboId)
                .quantity(quantity)
                .build();
    }
}