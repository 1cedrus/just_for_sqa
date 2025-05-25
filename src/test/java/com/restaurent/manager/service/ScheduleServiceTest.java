package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.ScheduleRequest;
import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.response.ScheduleResponse;
import com.restaurent.manager.dto.response.ScheduleTimeResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.enums.SCHEDULE_STATUS;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.*;
import com.restaurent.manager.service.impl.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TableRestaurantRepository tableRestaurantRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private TableTypeRepository tableTypeRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private DishCategoryRepository dishCategoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ScheduleDishRepository scheduleDishRepository;

    @MockBean
    private Clock clock;

    private Long restaurantId;
    private Restaurant restaurant;
    private TableRestaurant tableRestaurant;
    private Area area;
    private TableType tableType;
    private Dish dish;
    private DishCategory dishCategory;
    private Unit unit;
    private Customer customer;
    private Employee employee;
    private Role role;

    @BeforeEach
    void setup() {
        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
            fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

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
        restaurantId = restaurant.getId();

        // Create table type
        tableType = new TableType();
        tableType.setName("Standard Table");
        tableType = tableTypeRepository.saveAndFlush(tableType);

        // Create area
        area = new Area();
        area.setName("Main Area");
        area.setRestaurant(restaurant);
        area = areaRepository.saveAndFlush(area);

        // Create table
        tableRestaurant = new TableRestaurant();
        tableRestaurant.setName("Table 1");
        tableRestaurant.setArea(area);
        tableRestaurant.setTableType(tableType);
        tableRestaurant.setNumberChairs(4);
        tableRestaurant.setPositionX(0);
        tableRestaurant.setPositionY(0);
        tableRestaurant.setHidden(false);
        tableRestaurant = tableRestaurantRepository.saveAndFlush(tableRestaurant);

        // Create role
        role = new Role();
        role.setName("EMPLOYEE");
        role.setDescription("Employee role");
        role = roleRepository.saveAndFlush(role);

        // Create employee
        employee = new Employee();
        employee.setUsername("test_employee");
        employee.setPassword("password");
        employee.setEmployeeName("Test Employee");
        employee.setPhoneNumber("1234567890");
        employee.setRestaurant(restaurant);
        employee.setRole(role);
        employee = employeeRepository.saveAndFlush(employee);

        // Create customer
        customer = new Customer();
        customer.setName("Test Customer");
        customer.setPhoneNumber("123456789");
        customer.setAddress("Test Address");
        customer.setRestaurant(restaurant);
        customer.setCurrentPoint(0);
        customer.setTotalPoint(0);
        customer.setDateCreated(LocalDateTime.now(clock));
        customer = customerRepository.saveAndFlush(customer);

        // Create dish category
        dishCategory = new DishCategory();
        dishCategory.setName("Test Category");
        dishCategory.setRestaurant(restaurant);
        dishCategory = dishCategoryRepository.saveAndFlush(dishCategory);

        // Create unit
        unit = new Unit();
        unit.setName("Piece");
        unit.setHidden(false);
        unit = unitRepository.saveAndFlush(unit);

        // Create dish
        dish = new Dish();
        dish.setName("Test Dish");
        dish.setPrice(100.0);
        dish.setDescription("Test dish description");
        dish.setDishCategory(dishCategory);
        dish.setRestaurant(restaurant);
        dish.setUnit(unit);
        dish.setImageUrl("test_image.jpg");
        dish.setStatus(true);
        dish = dishRepository.saveAndFlush(dish);

    }

    // SS-10
    @Test
    void createScheduleShouldCreateSuccessfullyWhenDataIsValid() {
        ScheduleRequest scheduleRequest = new ScheduleRequest();
        scheduleRequest.setCustomerName("John Doe");
        scheduleRequest.setCustomerPhone("987654321");
        scheduleRequest.setBookedDate(LocalDate.now(clock).plusDays(1));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setNumbersOfCustomer(4);
        scheduleRequest.setTables(List.of(tableRestaurant.getId()));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(dish.getId()).quantity(2).build()));

        String result = scheduleService.createSchedule(restaurantId, scheduleRequest);

        assertEquals("success", result);

        // Verify in database
        List<Schedule> schedules = scheduleRepository.findByBookedDateAndRestaurant_IdAndStatus(
                scheduleRequest.getBookedDate(), restaurantId, SCHEDULE_STATUS.PENDING);
        assertEquals(1, schedules.size());

        Schedule createdSchedule = schedules.get(0);
        assertEquals("John Doe", createdSchedule.getCustomerName());
        assertEquals("987654321", createdSchedule.getCustomerPhone());
        assertEquals(LocalTime.of(12, 0), createdSchedule.getTime());
        assertEquals(LocalTime.of(13, 0), createdSchedule.getIntendTime());
        assertEquals(4, createdSchedule.getNumbersOfCustomer());
        assertEquals(SCHEDULE_STATUS.PENDING, createdSchedule.getStatus());
        assertTrue(createdSchedule.getTableRestaurants().contains(tableRestaurant));

        // Verify schedule dishes
        List<ScheduleDish> scheduleDishes = scheduleDishRepository.findBySchedule_Id(createdSchedule.getId());
        assertEquals(1, scheduleDishes.size());
        assertEquals(dish.getId(), scheduleDishes.get(0).getDish().getId());
        assertEquals(2, scheduleDishes.get(0).getQuantity());
    }

    // SS-11
    @Test
    void createScheduleShouldThrowExceptionWhenBookedDateIsInThePast() {
        ScheduleRequest scheduleRequest = new ScheduleRequest();
        scheduleRequest.setBookedDate(LocalDate.now(clock).minusDays(1));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(tableRestaurant.getId()));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(dish.getId()).build()));

        AppException e = assertThrows(AppException.class,
                () -> scheduleService.createSchedule(restaurantId, scheduleRequest));

        assertEquals(ErrorCode.TIME_INVALID, e.getErrorCode());

        // Verify no schedule was created
        List<Schedule> schedules = scheduleRepository.findByBookedDateAndRestaurant_IdAndStatus(
                scheduleRequest.getBookedDate(), restaurantId, SCHEDULE_STATUS.PENDING);
        assertTrue(schedules.isEmpty());
    }

    // SS-12
    @Test
    void createScheduleShouldThrowExceptionWhenBookedDateIsTodayAndTimeIsInThePast() {
        ScheduleRequest scheduleRequest = new ScheduleRequest();
        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("11:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(tableRestaurant.getId()));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(dish.getId()).build()));

        AppException e = assertThrows(AppException.class,
                () -> scheduleService.createSchedule(restaurantId, scheduleRequest));

        assertEquals(ErrorCode.TIME_INVALID, e.getErrorCode());

        // Verify no schedule was created
        List<Schedule> schedules = scheduleRepository.findByBookedDateAndRestaurant_IdAndStatus(
                LocalDate.now(clock), restaurantId, SCHEDULE_STATUS.PENDING);
        assertTrue(schedules.isEmpty());
    }

    // SS-13
    @Test
    void createScheduleShouldReturnErrorMessageWhenTablesAreNotAvailable() {
        // Create an existing schedule that conflicts
        Schedule existingSchedule = new Schedule();
        existingSchedule.setCustomerName("Existing Customer");
        existingSchedule.setCustomerPhone("111111111");
        existingSchedule.setBookedDate(LocalDate.now(clock));
        existingSchedule.setTime(LocalTime.of(12, 0));
        existingSchedule.setIntendTime(LocalTime.of(14, 0));
        existingSchedule.setNumbersOfCustomer(2);
        existingSchedule.setTableRestaurants(Set.of(tableRestaurant));
        existingSchedule.setRestaurant(restaurant);
        existingSchedule.setStatus(SCHEDULE_STATUS.PENDING);
        scheduleRepository.saveAndFlush(existingSchedule);

        ScheduleRequest scheduleRequest = new ScheduleRequest();
        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(120L);
        scheduleRequest.setTables(List.of(tableRestaurant.getId()));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(dish.getId()).build()));

        String result = scheduleService.createSchedule(restaurantId, scheduleRequest);

        assertEquals("Bàn Table 1 đã được đặt,  vui lòng chọn bàn khác hoặc khung giờ khác !", result);

        // Verify only the original schedule exists
        List<Schedule> schedules = scheduleRepository.findByBookedDateAndRestaurant_IdAndStatus(
                LocalDate.now(clock), restaurantId, SCHEDULE_STATUS.PENDING);
        assertEquals(1, schedules.size());
        assertEquals("Existing Customer", schedules.get(0).getCustomerName());
    }

    // SS-14
    @Test
    void checkTableIsBookedShouldReturnTrueWhenTableIsBooked() {
        // Create an existing schedule
        Schedule existingSchedule = new Schedule();
        existingSchedule.setCustomerName("Existing Customer");
        existingSchedule.setCustomerPhone("111111111");
        existingSchedule.setBookedDate(LocalDate.now(clock));
        existingSchedule.setTime(LocalTime.of(12, 0));
        existingSchedule.setIntendTime(LocalTime.of(14, 0));
        existingSchedule.setNumbersOfCustomer(2);
        existingSchedule.setTableRestaurants(Set.of(tableRestaurant));
        existingSchedule.setRestaurant(restaurant);
        existingSchedule.setStatus(SCHEDULE_STATUS.PENDING);
        scheduleRepository.saveAndFlush(existingSchedule);

        ScheduleRequest scheduleRequest = new ScheduleRequest();
        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(120L);
        scheduleRequest.setTables(List.of(tableRestaurant.getId()));

        boolean result = scheduleService.checkTableIsBooked(tableRestaurant.getId(), scheduleRequest);

        assertTrue(result);
    }

    // SS-15
    @Test
    void checkTableIsBookedShouldReturnFalseWhenTableIsNotBooked() {
        ScheduleRequest scheduleRequest = new ScheduleRequest();
        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(120L);
        scheduleRequest.setTables(List.of(tableRestaurant.getId()));

        boolean result = scheduleService.checkTableIsBooked(tableRestaurant.getId(), scheduleRequest);

        assertFalse(result);
    }

    // SS-16
    @Test
    void findScheduleRestaurantByDateShouldReturnSchedules() {
        LocalDate testDate = LocalDate.now(clock);

        // Create a schedule
        Schedule schedule = new Schedule();
        schedule.setCustomerName("Test Customer");
        schedule.setCustomerPhone("111111111");
        schedule.setBookedDate(testDate);
        schedule.setTime(LocalTime.of(12, 0));
        schedule.setIntendTime(LocalTime.of(13, 0));
        schedule.setNumbersOfCustomer(2);
        schedule.setTableRestaurants(Set.of(tableRestaurant));
        schedule.setRestaurant(restaurant);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule = scheduleRepository.saveAndFlush(schedule);

        // Create schedule dish
        ScheduleDish scheduleDish = new ScheduleDish();
        scheduleDish.setSchedule(schedule);
        scheduleDish.setDish(dish);
        scheduleDish.setQuantity(2);
        scheduleDishRepository.saveAndFlush(scheduleDish);

        List<ScheduleResponse> result = scheduleService.findScheduleRestaurantByDate(restaurantId, testDate);

        assertEquals(1, result.size());
        assertEquals(schedule.getId(), result.get(0).getId());
        assertEquals("Test Customer", result.get(0).getCustomerName());
        assertEquals(1, result.get(0).getDishes().size());
        assertEquals(dish.getId(), result.get(0).getDishes().get(0).getDish().getId());
    }

    // SS-17
    @Test
    void findScheduleRestaurantLateShouldReturnSchedules() {
        LocalDate today = LocalDate.now(clock);
        LocalTime pastTime = LocalTime.now(clock).minusHours(1);

        // Create a late schedule
        Schedule schedule = new Schedule();
        schedule.setCustomerName("Late Customer");
        schedule.setCustomerPhone("111111111");
        schedule.setBookedDate(today);
        schedule.setTime(pastTime);
        schedule.setIntendTime(pastTime.plusHours(1));
        schedule.setNumbersOfCustomer(2);
        schedule.setTableRestaurants(Set.of(tableRestaurant));
        schedule.setRestaurant(restaurant);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule = scheduleRepository.saveAndFlush(schedule);

        List<ScheduleResponse> result = scheduleService.findScheduleRestaurantLate(restaurantId);

        assertEquals(1, result.size());
        assertEquals(schedule.getId(), result.get(0).getId());
        assertEquals("Late Customer", result.get(0).getCustomerName());
    }

    // SS-18
    @Test
    void findScheduleRestaurantNearlyShouldReturnSchedules() {
        LocalDate today = LocalDate.now(clock);
        LocalTime nearTime = LocalTime.now(clock).plusMinutes(30);

        // Create a near schedule
        Schedule schedule = new Schedule();
        schedule.setCustomerName("Near Customer");
        schedule.setCustomerPhone("111111111");
        schedule.setBookedDate(today);
        schedule.setTime(nearTime);
        schedule.setIntendTime(nearTime.plusHours(1));
        schedule.setNumbersOfCustomer(2);
        schedule.setTableRestaurants(Set.of(tableRestaurant));
        schedule.setRestaurant(restaurant);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule = scheduleRepository.saveAndFlush(schedule);

        List<ScheduleResponse> result = scheduleService.findScheduleRestaurantNearly(restaurantId);

        assertEquals(1, result.size());
        assertEquals(schedule.getId(), result.get(0).getId());
        assertEquals("Near Customer", result.get(0).getCustomerName());
    }

    // SS-22
    @Test
    void updateStatusScheduleByIdShouldUpdateStatusSuccessfully() {
        LocalDate today = LocalDate.now(clock);

        // Create a schedule
        Schedule schedule = new Schedule();
        schedule.setCustomerName("Test Customer");
        schedule.setCustomerPhone("111111111");
        schedule.setBookedDate(today);
        schedule.setTime(LocalTime.of(12, 0));
        schedule.setIntendTime(LocalTime.of(13, 0));
        schedule.setNumbersOfCustomer(2);
        schedule.setTableRestaurants(new HashSet<>(List.of(tableRestaurant)));
        schedule.setRestaurant(restaurant);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule = scheduleRepository.saveAndFlush(schedule);

        scheduleService.updateStatusScheduleById(schedule.getId(), employee.getId(), SCHEDULE_STATUS.CANCEL);

        // Verify in database
        Optional<Schedule> updatedSchedule = scheduleRepository.findById(schedule.getId());
        assertTrue(updatedSchedule.isPresent());
        assertEquals(SCHEDULE_STATUS.CANCEL, updatedSchedule.get().getStatus());
    }

    // SS-23
    @Test
    void updateStatusScheduleByIdShouldThrowErrorWhenScheduleNotFound() {
        AppException e = assertThrows(AppException.class,
                () -> scheduleService.updateStatusScheduleById(999L, employee.getId(), SCHEDULE_STATUS.ACCEPT));

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // SS-30
    @Test
    void findByIdShouldReturnSchedule() {
        // Create a schedule
        Schedule schedule = new Schedule();
        schedule.setCustomerName("Test Customer");
        schedule.setCustomerPhone("111111111");
        schedule.setBookedDate(LocalDate.now(clock));
        schedule.setTime(LocalTime.of(12, 0));
        schedule.setIntendTime(LocalTime.of(13, 0));
        schedule.setNumbersOfCustomer(2);
        schedule.setTableRestaurants(Set.of(tableRestaurant));
        schedule.setRestaurant(restaurant);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule = scheduleRepository.saveAndFlush(schedule);

        Schedule result = scheduleService.findById(schedule.getId());

        assertEquals(schedule.getId(), result.getId());
        assertEquals("Test Customer", result.getCustomerName());
    }

    // SS-31
    @Test
    void findByIdShouldThrowErrorWhenScheduleNotFound() {
        AppException e = assertThrows(AppException.class, () -> scheduleService.findById(999L));

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // SS-32
    @Test
    void findSchedulesByTableIdShouldReturnPagingResult() {
        LocalDate today = LocalDate.now(clock);

        // Create a schedule
        Schedule schedule = new Schedule();
        schedule.setCustomerName("Test Customer");
        schedule.setCustomerPhone("111111111");
        schedule.setBookedDate(today);
        schedule.setTime(LocalTime.of(12, 0));
        schedule.setIntendTime(LocalTime.of(13, 0));
        schedule.setNumbersOfCustomer(2);
        schedule.setTableRestaurants(Set.of(tableRestaurant));
        schedule.setRestaurant(restaurant);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        scheduleRepository.saveAndFlush(schedule);

        Pageable pageable = PageRequest.of(0, 10);
        PagingResult<ScheduleResponse> result = scheduleService.findSchedulesByTableId(tableRestaurant.getId(),
                pageable);

        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals(1L, result.getTotalItems());
    }

    // SS-33
    @Test
    void getNumberScheduleRestaurantWithTimeShouldReturnScheduleCountsFor7Days() {
        LocalDate today = LocalDate.now(clock);

        // Create schedules for different days
        for (int i = 0; i < 3; i++) {
            Schedule schedule = new Schedule();
            schedule.setCustomerName("Customer " + i);
            schedule.setCustomerPhone("11111111" + i);
            schedule.setBookedDate(today.plusDays(i));
            schedule.setTime(LocalTime.of(12, 0));
            schedule.setIntendTime(LocalTime.of(13, 0));
            schedule.setNumbersOfCustomer(2);
            schedule.setTableRestaurants(Set.of(tableRestaurant));
            schedule.setRestaurant(restaurant);
            schedule.setStatus(SCHEDULE_STATUS.PENDING);
            scheduleRepository.saveAndFlush(schedule);
        }

        List<ScheduleTimeResponse> result = scheduleService.getNumberScheduleRestaurantWithTime(restaurantId);

        assertNotNull(result);
        assertEquals(7, result.size());

        // Check the first 3 days have schedules
        for (int i = 0; i < 3; i++) {
            assertEquals(today.plusDays(i), result.get(i).getDate());
            assertEquals(1, result.get(i).getNumbersSchedule());
        }

        // Check the remaining days have no schedules
        for (int i = 3; i < 7; i++) {
            assertEquals(today.plusDays(i), result.get(i).getDate());
            assertEquals(0, result.get(i).getNumbersSchedule());
        }
    }

    // SS-34
    @Test
    void findAllScheduleRestaurantShouldReturnCombinedPendingAndCancelSchedules() {
        // Create pending schedule
        Schedule pendingSchedule = new Schedule();
        pendingSchedule.setCustomerName("Pending Customer");
        pendingSchedule.setCustomerPhone("111111111");
        pendingSchedule.setBookedDate(LocalDate.now(clock));
        pendingSchedule.setTime(LocalTime.of(12, 0));
        pendingSchedule.setIntendTime(LocalTime.of(13, 0));
        pendingSchedule.setNumbersOfCustomer(2);
        pendingSchedule.setTableRestaurants(Set.of(tableRestaurant));
        pendingSchedule.setRestaurant(restaurant);
        pendingSchedule.setStatus(SCHEDULE_STATUS.PENDING);
        pendingSchedule = scheduleRepository.saveAndFlush(pendingSchedule);

        // Create cancelled schedule
        Schedule cancelledSchedule = new Schedule();
        cancelledSchedule.setCustomerName("Cancelled Customer");
        cancelledSchedule.setCustomerPhone("222222222");
        cancelledSchedule.setBookedDate(LocalDate.now(clock));
        cancelledSchedule.setTime(LocalTime.of(14, 0));
        cancelledSchedule.setIntendTime(LocalTime.of(15, 0));
        cancelledSchedule.setNumbersOfCustomer(3);
        cancelledSchedule.setTableRestaurants(Set.of(tableRestaurant));
        cancelledSchedule.setRestaurant(restaurant);
        cancelledSchedule.setStatus(SCHEDULE_STATUS.CANCEL);
        scheduleRepository.saveAndFlush(cancelledSchedule);

        // Create schedule dishes
        ScheduleDish scheduleDish1 = new ScheduleDish();
        scheduleDish1.setSchedule(pendingSchedule);
        scheduleDish1.setDish(dish);
        scheduleDish1.setQuantity(1);
        scheduleDishRepository.saveAndFlush(scheduleDish1);

        ScheduleDish scheduleDish2 = new ScheduleDish();
        scheduleDish2.setSchedule(cancelledSchedule);
        scheduleDish2.setDish(dish);
        scheduleDish2.setQuantity(2);
        scheduleDishRepository.saveAndFlush(scheduleDish2);

        Pageable pageable = PageRequest.of(0, 10);
        List<ScheduleResponse> result = scheduleService.findAllScheduleRestaurant(restaurantId, pageable);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify both schedules are returned
        boolean foundPending = false;
        boolean foundCancelled = false;

        for (ScheduleResponse response : result) {
            if (response.getCustomerName().equals("Pending Customer")) {
                foundPending = true;
                assertEquals(SCHEDULE_STATUS.PENDING, response.getStatus());
                assertEquals(1, response.getDishes().size());
            } else if (response.getCustomerName().equals("Cancelled Customer")) {
                foundCancelled = true;
                assertEquals(SCHEDULE_STATUS.CANCEL, response.getStatus());
                assertEquals(1, response.getDishes().size());
            }
        }

        assertTrue(foundPending);
        assertTrue(foundCancelled);
    }
}
