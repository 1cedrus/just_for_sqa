package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.UnitRequest;
import com.restaurent.manager.dto.response.UnitResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Unit;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.DishCategoryRepository;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.UnitRepository;
import com.restaurent.manager.service.impl.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
class UnitServiceTest {

    @Autowired
    private UnitService unitService;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DishCategoryRepository dishCategoryRepository;

    @MockBean
    private Clock clock;

    private Unit unit;
    private Account account;
    private Restaurant restaurant;
    private DishCategory dishCategory;

    @BeforeEach
    void setup() {
        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
                fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // Create account
        account = new Account();
        account.setUsername("test_account");
        account.setPassword("password");
        account.setEmail("test@example.com");
        account = accountRepository.saveAndFlush(account);

        // Create restaurant
        restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurant.setProvince("Test Province");
        restaurant.setDistrict("Test District");
        restaurant.setMoneyToPoint(1.0);
        restaurant.setPointToMoney(1.0);
        restaurant.setMonthsRegister(12);
        restaurant.setVatActive(false);
        restaurant.setDateCreated(LocalDate.now(clock));
        restaurant = restaurantRepository.saveAndFlush(restaurant);

        // Create dish category
        dishCategory = new DishCategory();
        dishCategory.setName("Test Category");
        dishCategory.setRestaurant(restaurant);
        dishCategory = dishCategoryRepository.saveAndFlush(dishCategory);

        // Create unit
        unit = new Unit();
        unit.setName("Test Unit");
        unit.setAccount(account);
        unit.setHidden(false);
        unit = unitRepository.saveAndFlush(unit);
    }

    // US-1
    @Test
    void createUnitShouldReturnUnitResponse() {
        UnitRequest request = new UnitRequest();
        request.setName("New Unit");
        request.setAccountId(account.getId());

        UnitResponse result = unitService.createUnit(request);

        assertNotNull(result);
        assertEquals("New Unit", result.getName());
        assertNotNull(result.getId());

        // Verify in database
        List<Unit> units = unitRepository.getUnitsByAccount_Id(account.getId());
        assertTrue(units.stream().anyMatch(u -> "New Unit".equals(u.getName())));
    }

    // US-2
    @Test
    void createUnitShouldThrowExceptionWhenAccountNotFound() {
        UnitRequest request = new UnitRequest();
        request.setName("New Unit");
        request.setAccountId(999L);

        AppException e = assertThrows(AppException.class, () -> {
            unitService.createUnit(request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // US-3
    @Test
    void getUnitsByAccountIdShouldReturnListOfUnitResponse() {
        List<UnitResponse> result = unitService.getUnitsByAccountId(account.getId());

        assertNotNull(result);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().anyMatch(u -> "Test Unit".equals(u.getName())));
    }

    // US-4
    @Test
    void updateUnitShouldReturnUpdatedUnitResponse() {
        UnitRequest request = new UnitRequest();
        request.setName("Updated Unit");
        request.setAccountId(account.getId());

        UnitResponse result = unitService.updateUnit(unit.getId(), request);

        assertNotNull(result);
        assertEquals(unit.getId(), result.getId());
        assertEquals("Updated Unit", result.getName());

        // Verify in database
        Unit updatedUnit = unitRepository.findById(unit.getId()).orElse(null);
        assertNotNull(updatedUnit);
        assertEquals("Updated Unit", updatedUnit.getName());
    }

    // US-5
    @Test
    void updateUnitShouldThrowExceptionWhenUnitNotFound() {
        UnitRequest request = new UnitRequest();
        request.setName("Updated Unit");
        request.setAccountId(account.getId());

        AppException e = assertThrows(AppException.class, () -> {
            unitService.updateUnit(999L, request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // US-6
    @Test
    void deleteUnitByIdShouldDeleteUnitWhenNoDishExists() {
        unitService.deleteUnitById(unit.getId());

        // Verify in database - unit should be deleted
        assertFalse(unitRepository.findById(unit.getId()).isPresent());
    }

    // US-7
    @Test
    void deleteUnitByIdShouldSetHiddenWhenDishExists() {
        // Create a dish that uses this unit
        Dish dish = new Dish();
        dish.setName("Test Dish");
        dish.setPrice(100.0);
        dish.setDescription("Test dish description");
        dish.setDishCategory(dishCategory);
        dish.setRestaurant(restaurant);
        dish.setUnit(unit);
        dish.setImageUrl("test_image.jpg");
        dish.setStatus(true);
        dishRepository.saveAndFlush(dish);

        unitService.deleteUnitById(unit.getId());

        // Verify in database - unit should be hidden, not deleted
        Unit updatedUnit = unitRepository.findById(unit.getId()).orElse(null);
        assertNotNull(updatedUnit);
        assertTrue(updatedUnit.isHidden());
    }

    // US-8
    @Test
    void deleteUnitByIdShouldThrowExceptionWhenUnitNotFound() {
        AppException e = assertThrows(AppException.class, () -> {
            unitService.deleteUnitById(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // US-9
    @Test
    void findByIdShouldReturnUnitWhenExists() {
        Unit result = unitService.findById(unit.getId());

        assertNotNull(result);
        assertEquals(unit.getId(), result.getId());
        assertEquals("Test Unit", result.getName());
    }

    // US-10
    @Test
    void findByIdShouldThrowExceptionWhenNotExists() {
        AppException e = assertThrows(AppException.class, () -> {
            unitService.findById(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }
}
