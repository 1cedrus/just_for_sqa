package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.ScheduleRequest;
import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.dto.response.DishResponse;
import com.restaurent.manager.dto.response.ScheduleDishResponse;
import com.restaurent.manager.dto.response.ScheduleResponse;
import com.restaurent.manager.dto.response.ScheduleTimeResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.enums.SCHEDULE_STATUS;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.ScheduleMapper;
import com.restaurent.manager.mapper.ScheduleMapperImpl;
import com.restaurent.manager.repository.CustomerRepository;
import com.restaurent.manager.repository.ScheduleRepository;
import com.restaurent.manager.repository.TableRestaurantRepository;
import com.restaurent.manager.service.impl.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScheduleServiceTest {

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    ITableRestaurantService tableRestaurantService;

    @Mock
    TableRestaurantRepository tableRestaurantRepository;

    @Mock
    IRestaurantService restaurantService;

    @Mock
    IScheduleDishService scheduleDishService;

    @Mock
    ICustomerService customerService;

    @Mock
    IEmployeeService employeeService;

    @Mock
    IOrderService orderService;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    Clock clock;

    @Spy
    ScheduleMapper scheduleMapper = new ScheduleMapperImpl();

    @InjectMocks
    ScheduleService scheduleService;

    Long restaurantId = 1L;
    ScheduleRequest scheduleRequest;
    TableRestaurant tableRestaurant;
    Restaurant restaurant;
    Customer customer;
    Employee employee;

    LocalDate bookedDate;
    LocalTime bookedTime;
    LocalDateTime bookedDateTime;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        scheduleRequest = new ScheduleRequest();
        bookedDate = LocalDate.of(2025, 4, 8);
        bookedTime = LocalTime.of(12, 0);
        bookedDateTime = LocalDateTime.of(bookedDate, bookedTime);

        tableRestaurant = new TableRestaurant();
        restaurant = new Restaurant();
        customer = new Customer();
        employee = new Employee();

        restaurant.setId(restaurantId);

        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
            fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    void createScheduleShouldCreateSuccessfullyWhenDataIsValid() {
        when(tableRestaurantService.findById(1L)).thenReturn(new TableRestaurant());
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(new Restaurant());
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scheduleRequest.setBookedDate(LocalDate.now().plusDays(1));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        String result = scheduleService.createSchedule(restaurantId, scheduleRequest);

        assertEquals("success", result);

        verify(scheduleRepository).save(any(Schedule.class));
        verify(scheduleDishService).createScheduleDish(any(Schedule.class), any(DishOrderRequest.class));
    }

    @Test
    void createScheduleShouldThrowExceptionWhenBookedDateIsInThePast() {
        scheduleRequest.setBookedDate(LocalDate.now(clock).minusDays(1));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        AppException e = assertThrows(AppException.class, () -> scheduleService.createSchedule(restaurantId, scheduleRequest));

        assertEquals(ErrorCode.TIME_INVALID, e.getErrorCode());

        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void createScheduleShouldThrowExceptionWhenBookedDateIsTodayAndTimeIsInThePast() {
        scheduleRequest.setBookedDate(LocalDate.now(clock).minusDays(1));
        scheduleRequest.setTime("11:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        AppException e = assertThrows(AppException.class, () -> scheduleService.createSchedule(restaurantId, scheduleRequest));

        assertEquals(ErrorCode.TIME_INVALID, e.getErrorCode());

        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void createScheduleShouldThrowExceptionWhenTablesAreNotAvailable() {
        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(120L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));
        tableRestaurant.setId(1L);
        tableRestaurant.setName("Table 1");

        when(scheduleRepository.findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"))).thenReturn(List.of(new Schedule()));
        when(tableRestaurantService.findById(1L)).thenReturn(tableRestaurant);

        String result = scheduleService.createSchedule(restaurantId, scheduleRequest);

        assertEquals("Bàn Table 1 đã được đặt,  vui lòng chọn bàn khác hoặc khung giờ khác !", result);

        verify(tableRestaurantService).findById(1L);
        verify(scheduleRepository).findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"));
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void checkTableIsBookedShouldReturnTrueWhenTableIsBooked() {
        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(120L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));
        tableRestaurant.setId(1L);
        tableRestaurant.setName("Table 1");

        when(scheduleRepository.findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"))).thenReturn(List.of(new Schedule()));

        boolean result = scheduleService.checkTableIsBooked(restaurantId, scheduleRequest);

        assertTrue(result);

        verify(scheduleRepository).findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"));
    }

    @Test
    void checkTableIsBookedShouldReturnFalseWhenTableIsNotBooked() {
        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(120L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));
        tableRestaurant.setId(1L);
        tableRestaurant.setName("Table 1");

        when(scheduleRepository.findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"))).thenReturn(List.of());

        boolean result = scheduleService.checkTableIsBooked(restaurantId, scheduleRequest);

        assertFalse(result);

        verify(scheduleRepository).findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"));
    }

    @Test
    void findScheduleRestaurantByDateShouldReturnSchedules() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);

        List<Schedule> testSchedules = List.of(schedule);

        when(scheduleRepository.findByBookedDateAndRestaurant_IdAndStatus(bookedDate, restaurantId, SCHEDULE_STATUS.PENDING)).thenReturn(testSchedules);
        when(scheduleDishService.findDishOrComboBySchedule(1L)).thenReturn(List.of(ScheduleDishResponse.builder().id(1L).build()));

        List<ScheduleResponse> result = scheduleService.findScheduleRestaurantByDate(restaurantId, bookedDate);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getDishes().get(0).getId());

        verify(scheduleRepository).findByBookedDateAndRestaurant_IdAndStatus(bookedDate, restaurantId, SCHEDULE_STATUS.PENDING);
        verify(scheduleDishService).findDishOrComboBySchedule(1L);
    }

    @Test
    void findScheduleRestaurantLateShouldReturnSchedules() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);

        List<Schedule> testSchedules = List.of(schedule);

        when(scheduleRepository.findByRestaurant_IdAndBookedDateAndTimeIsBeforeAndStatus(restaurantId, bookedDate, bookedTime, SCHEDULE_STATUS.PENDING)).thenReturn(testSchedules);
        when(scheduleDishService.findDishOrComboBySchedule(1L)).thenReturn(List.of(ScheduleDishResponse.builder().id(1L).build()));

        List<ScheduleResponse> result = scheduleService.findScheduleRestaurantLate(restaurantId);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getDishes().get(0).getId());

        verify(scheduleRepository).findByRestaurant_IdAndBookedDateAndTimeIsBeforeAndStatus(restaurantId, bookedDate, bookedTime, SCHEDULE_STATUS.PENDING);
        verify(scheduleDishService).findDishOrComboBySchedule(1L);
    }

    @Test
    void findScheduleRestaurantNearlyShouldReturnSchedules() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);

        List<Schedule> testSchedules = List.of(schedule);

        when(scheduleRepository.findByRestaurant_IdAndBookedDateAndTimeBetweenAndStatus(restaurantId, bookedDate, bookedTime, bookedTime.plusHours(1), SCHEDULE_STATUS.PENDING)).thenReturn(testSchedules);
        when(scheduleDishService.findDishOrComboBySchedule(1L)).thenReturn(List.of(ScheduleDishResponse.builder().id(1L).build()));

        List<ScheduleResponse> result = scheduleService.findScheduleRestaurantNearly(restaurantId);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getDishes().get(0).getId());

        verify(scheduleRepository).findByRestaurant_IdAndBookedDateAndTimeBetweenAndStatus(restaurantId, bookedDate, bookedTime, bookedTime.plusHours(1), SCHEDULE_STATUS.PENDING);
        verify(scheduleDishService).findDishOrComboBySchedule(1L);
    }

    @Test
    void customerReceiveBookTableShouldWorkProperlyWhenCustomerExisted() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        DishResponse dish = new DishResponse();
        dish.setId(1L);
        ComboResponse combo = new ComboResponse();
        combo.setId(2L);

        when(customerService.existCustomerByPhoneNumberAndRestaurantId(schedule.getCustomerPhone(), schedule.getRestaurant().getId())).thenReturn(true);
        when(customerService.findCustomerByPhoneNumber(schedule.getCustomerPhone(), schedule.getRestaurant().getId())).thenReturn(customer);
        when(employeeService.findEmployeeById(1L)).thenReturn(employee);
        when(scheduleDishService.findDishOrComboBySchedule(1L)).thenReturn(List.of(
            ScheduleDishResponse.builder().id(1L).dish(dish).quantity(1).build(),
            ScheduleDishResponse.builder().id(2L).combo(combo).quantity(2).build()
        ));

        scheduleService.customerReceiveBookTable(1L, schedule);

        verify(customerService).findCustomerByPhoneNumber(schedule.getCustomerPhone(), schedule.getRestaurant().getId());
        verify(employeeService).findEmployeeById(1L);
        verify(scheduleDishService).findDishOrComboBySchedule(1L);
        verify(orderService).createOrder(customer, employee, tableRestaurant, restaurant);
        verify(orderService).addDishToOrder(0L, List.of(
            DishOrderRequest.builder().dishId(dish.getId()).quantity(1).build(),
            DishOrderRequest.builder().comboId(combo.getId()).quantity(2).build()
        ));
    }

    @Test
    void customerReceiveBookTableShouldWorkProperlyWhenCustomerNotExisted() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        DishResponse dish = new DishResponse();
        dish.setId(1L);
        ComboResponse combo = new ComboResponse();
        combo.setId(2L);

        when(customerService.existCustomerByPhoneNumberAndRestaurantId(schedule.getCustomerPhone(), schedule.getRestaurant().getId())).thenReturn(false);
        when(employeeService.findEmployeeById(1L)).thenReturn(employee);
        when(scheduleDishService.findDishOrComboBySchedule(1L)).thenReturn(List.of(
            ScheduleDishResponse.builder().id(1L).dish(dish).quantity(1).build(),
            ScheduleDishResponse.builder().id(2L).combo(combo).quantity(2).build()
        ));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scheduleService.customerReceiveBookTable(1L, schedule);

        verify(customerRepository).save(Customer.builder()
            .name(schedule.getCustomerName())
            .phoneNumber(schedule.getCustomerPhone())
            .restaurant(schedule.getRestaurant())
            .dateCreated(bookedDateTime)
            .build());
        verify(employeeService).findEmployeeById(1L);
        verify(scheduleDishService).findDishOrComboBySchedule(1L);
        verify(orderService).createOrder(Customer.builder()
            .name(schedule.getCustomerName())
            .phoneNumber(schedule.getCustomerPhone())
            .restaurant(schedule.getRestaurant())
            .dateCreated(bookedDateTime)
            .build(), employee, tableRestaurant, restaurant);
        verify(orderService).addDishToOrder(0L, List.of(
            DishOrderRequest.builder().dishId(dish.getId()).quantity(1).build(),
            DishOrderRequest.builder().comboId(combo.getId()).quantity(2).build()
        ));
    }

    @Test
    void customerReceiveBookTableShouldThrowErrorIfTableNotAvailable() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        tableRestaurant.setOrderCurrent(1L);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        when(customerService.existCustomerByPhoneNumberAndRestaurantId(schedule.getCustomerPhone(), schedule.getRestaurant().getId())).thenReturn(true);
        when(employeeService.findEmployeeById(1L)).thenReturn(employee);

        AppException e = assertThrows(AppException.class, () -> scheduleService.customerReceiveBookTable(1L, schedule));

        assertEquals(ErrorCode.TABLE_NOT_FREE, e.getErrorCode());

        verify(customerService).existCustomerByPhoneNumberAndRestaurantId(schedule.getCustomerPhone(), schedule.getRestaurant().getId());
    }

    @Test
    void updateStatusScheduleByIdShouldUpdateStatusSuccessfully() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule));

        scheduleService.updateStatusScheduleById(1L, 1L, SCHEDULE_STATUS.ACCEPT);

        assertEquals(SCHEDULE_STATUS.ACCEPT, schedule.getStatus());

        verify(scheduleRepository).save(schedule);
    }

    @Test
    void updateStatusScheduleByIdShouldThrowErrorWhenScheduleNotFound() {
        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        AppException e = assertThrows(AppException.class, () -> scheduleService.updateStatusScheduleById(1L, 1L, SCHEDULE_STATUS.ACCEPT));

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(scheduleRepository).findById(1L);
    }

    @Test
    void updateStatusScheduleByIdShouldThrowErrorWhenScheduleIsNotPending() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate.plusDays(1));
        schedule.setStatus(SCHEDULE_STATUS.CANCEL);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule));

        AppException e = assertThrows(AppException.class, () -> scheduleService.updateStatusScheduleById(1L, 1L, SCHEDULE_STATUS.ACCEPT));

        assertEquals(ErrorCode.NOT_TODAY, e.getErrorCode());

        verify(scheduleRepository).findById(1L);
    }

    @Test
    void updateScheduleRestaurantShouldUpdateSuccessfullyWhenDataValid() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule));
        when(tableRestaurantService.findById(1L)).thenReturn(tableRestaurant);

        scheduleRequest.setBookedDate(LocalDate.now().plusDays(1));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        String result = scheduleService.updateScheduleRestaurant(1L, scheduleRequest);

        assertEquals("success", result);

        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void updateScheduleRestaurantShouldThrowErrorWhenScheduleNotFound() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        scheduleRequest.setBookedDate(LocalDate.now().minusDays(1));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        AppException e = assertThrows(AppException.class, () -> scheduleService.updateScheduleRestaurant(1L, scheduleRequest));

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(scheduleRepository).findById(1L);
    }

    @Test
    void updateScheduleRestaurantShouldThrowErrorWhenDateIsInThePast() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        scheduleRequest.setBookedDate(LocalDate.now(clock).minusDays(1));
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        AppException e = assertThrows(AppException.class, () -> scheduleService.updateScheduleRestaurant(1L, scheduleRequest));

        assertEquals(ErrorCode.TIME_INVALID, e.getErrorCode());
    }

    @Test
    void updateScheduleRestaurantShouldThrowErrorWhenDateIsTodayAndTimeIsInThePast() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        scheduleRequest.setBookedDate(LocalDate.now(clock));
        scheduleRequest.setTime("11:00");
        scheduleRequest.setIntendTimeMinutes(60L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        AppException e = assertThrows(AppException.class, () -> scheduleService.updateScheduleRestaurant(1L, scheduleRequest));

        assertEquals(ErrorCode.TIME_INVALID, e.getErrorCode());
    }

    @Test
    void updateScheduleRestaurantShouldThrowErrorWhenTablesAreNotAvailable() {
        tableRestaurant.setName("Table 1");

        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        scheduleRequest.setBookedDate(bookedDate);
        scheduleRequest.setTime("12:00");
        scheduleRequest.setIntendTimeMinutes(120L);
        scheduleRequest.setTables(List.of(1L));
        scheduleRequest.setScheduleDishes(List.of(DishOrderRequest.builder().dishId(1L).build()));

        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule));
        when(tableRestaurantService.findById(1L)).thenReturn(tableRestaurant);

        when(scheduleRepository.findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"))).thenReturn(List.of(new Schedule()));
        when(tableRestaurantService.findById(1L)).thenReturn(tableRestaurant);

        String result = scheduleService.updateScheduleRestaurant(1L, scheduleRequest);

        assertEquals("Bàn Table 1 đã được đặt,  vui lòng chọn bàn khác hoặc khung giờ khác !", result);

        verify(tableRestaurantService).findById(1L);
        verify(scheduleRepository).findSchedulesByTableAndDateRange(1L, bookedDate, LocalTime.parse("12:00"), LocalTime.parse("14:00"));
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }


    @Test
    void findByIdAndRestaurantIdShouldReturnSchedule() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule));

        Schedule result = scheduleService.findById(1L);

        assertEquals(1L, result.getId());

        verify(scheduleRepository).findById(1L);
    }

    @Test
    void findByIdAndRestaurantIdShouldThrowErrorWhenScheduleNotFound() {
        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        AppException e = assertThrows(AppException.class, () -> scheduleService.findById(1L));

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(scheduleRepository).findById(1L);
    }

    @Test
    void findSchedulesByTableIdShouldReturnPagingResult() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        Pageable pageable = PageRequest.of(0, 10);

        when(scheduleRepository.findSchedulesByTableIdAndDate(1L, bookedDate, pageable))
            .thenReturn(List.of(schedule));
        when(scheduleRepository.countSchedulesByTableIdAndDate(1L, bookedDate)).thenReturn(1);

        PagingResult<ScheduleResponse> result = scheduleService.findSchedulesByTableId(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals(1L, result.getTotalItems());

        verify(scheduleRepository).findSchedulesByTableIdAndDate(1L, bookedDate, pageable);
        verify(scheduleMapper).toScheduleResponse(schedule);
    }

    @Test
    void getNumberScheduleRestaurantWithTimeShouldReturnScheduleCountsFor7Days() {
        when(scheduleRepository.countByRestaurant_IdAndBookedDateAndStatus(eq(restaurantId), any(LocalDate.class), eq(SCHEDULE_STATUS.PENDING))).thenReturn(2);

        List<ScheduleTimeResponse> result = scheduleService.getNumberScheduleRestaurantWithTime(restaurantId);

        assertNotNull(result);
        assertEquals(7, result.size());

        for (int i = 0; i < 7; i++) {
            ScheduleTimeResponse response = result.get(i);
            assertEquals(bookedDate.plusDays(i), response.getDate());
            assertEquals(2L, response.getNumbersSchedule());
        }

        verify(scheduleRepository, times(7))
            .countByRestaurant_IdAndBookedDateAndStatus(eq(restaurantId), any(LocalDate.class), eq(SCHEDULE_STATUS.PENDING));
    }

    @Test
    void findAllScheduleRestaurantShouldReturnCombinedPendingAndCancelSchedules() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setBookedDate(bookedDate);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule.setCustomerPhone("123456789");
        schedule.setRestaurant(restaurant);
        schedule.setTableRestaurants(Set.of(tableRestaurant));

        Pageable pageable = PageRequest.of(0, 10);

        when(scheduleRepository.findByRestaurant_IdAndStatus(restaurantId, pageable, SCHEDULE_STATUS.PENDING))
            .thenReturn(List.of(schedule));
        when(scheduleRepository.findByRestaurant_IdAndStatus(restaurantId, pageable, SCHEDULE_STATUS.CANCEL))
            .thenReturn(List.of());

        when(scheduleDishService.findDishOrComboBySchedule(1L)).thenReturn(List.of(
            ScheduleDishResponse.builder().id(1L).dish(new DishResponse()).quantity(1).build(),
            ScheduleDishResponse.builder().id(2L).combo(new ComboResponse()).quantity(2).build()
        ));

        List<ScheduleResponse> result = scheduleService.findAllScheduleRestaurant(restaurantId, pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getDishes().get(0).getId());

        verify(scheduleRepository).findByRestaurant_IdAndStatus(restaurantId, pageable, SCHEDULE_STATUS.PENDING);
        verify(scheduleRepository).findByRestaurant_IdAndStatus(restaurantId, pageable, SCHEDULE_STATUS.CANCEL);
        verify(scheduleDishService).findDishOrComboBySchedule(1L);
    }
}
