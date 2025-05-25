package com.restaurent.manager.service;

import com.restaurent.manager.dto.response.StatisticChartValueManager;
import com.restaurent.manager.dto.response.StatisticResponse;
import com.restaurent.manager.dto.response.StatisticTableResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.repository.*;
import com.restaurent.manager.service.impl.BillService;
import com.restaurent.manager.service.impl.StatisticService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
class StatisticServiceTest {

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BillRepository billRepository;

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
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private Clock clock;

    private Long restaurantId;
    private Restaurant restaurant;
    private Customer customer1;
    private Customer customer2;
    private Bill bill1;
    private Bill bill2;
    private Bill bill3;
    private TableRestaurant tableRestaurant;
    private Area area;
    private TableType tableType;
    private Dish dish;
    private DishCategory dishCategory;
    private Unit unit;
    private Employee employee;
    private Role role;

    private LocalDate mockedDate;
    private LocalTime mockedTime;
    private LocalDateTime mockedDateTime;

    @BeforeEach
    void setup() {
        mockedDate = LocalDate.of(2025, 4, 8);
        mockedTime = LocalTime.of(12, 0);
        mockedDateTime = LocalDateTime.of(mockedDate, mockedTime);

        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
                fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
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

        // Create customers
        customer1 = new Customer();
        customer1.setName("Test Customer 1");
        customer1.setPhoneNumber("123456789");
        customer1.setAddress("Test Address 1");
        customer1.setRestaurant(restaurant);
        customer1.setCurrentPoint(0);
        customer1.setTotalPoint(0);
        customer1.setDateCreated(mockedDateTime);
        customer1 = customerRepository.saveAndFlush(customer1);

        customer2 = new Customer();
        customer2.setName("Test Customer 2");
        customer2.setPhoneNumber("987654321");
        customer2.setAddress("Test Address 2");
        customer2.setRestaurant(restaurant);
        customer2.setCurrentPoint(0);
        customer2.setTotalPoint(0);
        customer2.setDateCreated(mockedDateTime);
        customer2 = customerRepository.saveAndFlush(customer2);
    }

    // SS-1
    @Test
    void getStatisticRestaurantByIdShouldReturnStatsForToday() {
        // Create orders for today
        Order order1 = new Order();
        order1.setRestaurant(restaurant);
        order1.setCustomer(customer1);
        order1.setEmployee(employee);
        order1.setTableRestaurant(tableRestaurant);
        order1.setOrderDate(mockedDate);
        order1 = orderRepository.saveAndFlush(order1);

        Order order2 = new Order();
        order2.setRestaurant(restaurant);
        order2.setCustomer(customer2);
        order2.setEmployee(employee);
        order2.setTableRestaurant(tableRestaurant);
        order2.setOrderDate(mockedDate);
        order2 = orderRepository.saveAndFlush(order2);

        // Create bills for today
        bill1 = new Bill();
        bill1.setOrder(order1);
        bill1.setDateCreated(mockedDateTime);
        bill1.setTotal(1000.0);
        bill1.setPointUsed(0.0);
        bill1 = billRepository.saveAndFlush(bill1);

        bill2 = new Bill();
        bill2.setOrder(order2);
        bill2.setDateCreated(mockedDateTime);
        bill2.setTotal(500.0);
        bill2.setPointUsed(0.0);
        bill2 = billRepository.saveAndFlush(bill2);

        StatisticResponse result = statisticService.getStatisticRestaurantById(restaurantId, "1");

        assertNotNull(result);
        assertEquals(0, result.getNumbersCustomer());
        assertEquals(2, result.getNumbersBill());
        // Note: Profit and VAT calculations depend on the actual implementation of
        // billService
        assertNotNull(result.getProfit());
        assertNotNull(result.getVat());
    }

    // SS-2
    @Test
    void getStatisticRestaurantByIdShouldReturnStatsForYesterday() {
        LocalDate yesterday = mockedDate.minusDays(1);
        LocalDateTime yesterdayDateTime = yesterday.atTime(mockedTime);

        // Create customer for yesterday
        Customer yesterdayCustomer = new Customer();
        yesterdayCustomer.setName("Yesterday Customer");
        yesterdayCustomer.setPhoneNumber("111111111");
        yesterdayCustomer.setAddress("Yesterday Address");
        yesterdayCustomer.setRestaurant(restaurant);
        yesterdayCustomer.setCurrentPoint(0);
        yesterdayCustomer.setTotalPoint(0);
        yesterdayCustomer.setDateCreated(yesterdayDateTime);
        yesterdayCustomer = customerRepository.saveAndFlush(yesterdayCustomer);

        // Create order for yesterday
        Order yesterdayOrder = new Order();
        yesterdayOrder.setRestaurant(restaurant);
        yesterdayOrder.setCustomer(yesterdayCustomer);
        yesterdayOrder.setEmployee(employee);
        yesterdayOrder.setTableRestaurant(tableRestaurant);
        yesterdayOrder.setOrderDate(yesterday);
        yesterdayOrder = orderRepository.saveAndFlush(yesterdayOrder);

        // Create bill for yesterday
        Bill yesterdayBill = new Bill();
        yesterdayBill.setOrder(yesterdayOrder);
        yesterdayBill.setDateCreated(yesterdayDateTime);
        yesterdayBill.setTotal(500.0);
        yesterdayBill.setPointUsed(0.0);
        billRepository.saveAndFlush(yesterdayBill);

        StatisticResponse result = statisticService.getStatisticRestaurantById(restaurantId, "-1");

        assertNotNull(result);
        assertEquals(1, result.getNumbersCustomer());
        assertEquals(1, result.getNumbersBill());
        assertNotNull(result.getProfit());
        assertNotNull(result.getVat());
    }

    // SS-3
    @Test
    void getStatisticRestaurantByIdShouldReturnNullForInvalidDay() {
        StatisticResponse result = statisticService.getStatisticRestaurantById(restaurantId, "0");

        assertNull(result);
    }

    // SS-4
    @Test
    void getStatisticByRestaurantIdBetweenStartDayToEndDayShouldReturnStats() {
        LocalDateTime start = mockedDateTime.minusDays(2);
        LocalDateTime end = mockedDateTime;

        // Create customers for the date range
        Customer rangeCustomer1 = new Customer();
        rangeCustomer1.setName("Range Customer 1");
        rangeCustomer1.setPhoneNumber("222222222");
        rangeCustomer1.setAddress("Range Address 1");
        rangeCustomer1.setRestaurant(restaurant);
        rangeCustomer1.setCurrentPoint(0);
        rangeCustomer1.setTotalPoint(0);
        rangeCustomer1.setDateCreated(start);
        rangeCustomer1 = customerRepository.saveAndFlush(rangeCustomer1);

        Customer rangeCustomer2 = new Customer();
        rangeCustomer2.setName("Range Customer 2");
        rangeCustomer2.setPhoneNumber("333333333");
        rangeCustomer2.setAddress("Range Address 2");
        rangeCustomer2.setRestaurant(restaurant);
        rangeCustomer2.setCurrentPoint(0);
        rangeCustomer2.setTotalPoint(0);
        rangeCustomer2.setDateCreated(end);
        rangeCustomer2 = customerRepository.saveAndFlush(rangeCustomer2);

        // Create orders for the date range
        Order rangeOrder1 = new Order();
        rangeOrder1.setRestaurant(restaurant);
        rangeOrder1.setCustomer(rangeCustomer1);
        rangeOrder1.setEmployee(employee);
        rangeOrder1.setTableRestaurant(tableRestaurant);
        rangeOrder1.setOrderDate(start.toLocalDate());
        rangeOrder1 = orderRepository.saveAndFlush(rangeOrder1);

        Order rangeOrder2 = new Order();
        rangeOrder2.setRestaurant(restaurant);
        rangeOrder2.setCustomer(rangeCustomer2);
        rangeOrder2.setEmployee(employee);
        rangeOrder2.setTableRestaurant(tableRestaurant);
        rangeOrder2.setOrderDate(end.toLocalDate());
        rangeOrder2 = orderRepository.saveAndFlush(rangeOrder2);

        Order rangeOrder3 = new Order();
        rangeOrder3.setRestaurant(restaurant);
        rangeOrder3.setCustomer(rangeCustomer1);
        rangeOrder3.setEmployee(employee);
        rangeOrder3.setTableRestaurant(tableRestaurant);
        rangeOrder3.setOrderDate(start.plusDays(1).toLocalDate());
        rangeOrder3 = orderRepository.saveAndFlush(rangeOrder3);

        // Create bills for the date range
        Bill rangeBill1 = new Bill();
        rangeBill1.setOrder(rangeOrder1);
        rangeBill1.setDateCreated(start);
        rangeBill1.setTotal(500.0);
        rangeBill1.setPointUsed(0.0);
        billRepository.saveAndFlush(rangeBill1);

        Bill rangeBill2 = new Bill();
        rangeBill2.setOrder(rangeOrder2);
        rangeBill2.setDateCreated(end);
        rangeBill2.setTotal(1000.0);
        rangeBill2.setPointUsed(0.0);
        billRepository.saveAndFlush(rangeBill2);

        Bill rangeBill3 = new Bill();
        rangeBill3.setOrder(rangeOrder3);
        rangeBill3.setDateCreated(start.plusDays(1));
        rangeBill3.setTotal(750.0);
        rangeBill3.setPointUsed(0.0);
        billRepository.saveAndFlush(rangeBill3);

        StatisticResponse result = statisticService.getStatisticByRestaurantIdBetweenStartDayToEndDay(restaurantId,
                start, end);

        assertNotNull(result);
        assertEquals(4, result.getNumbersCustomer());
        assertEquals(3, result.getNumbersBill());
        assertNotNull(result.getProfit());
        assertNotNull(result.getVat());
    }

    // SS-5
    @Test
    void getDetailStatisticRestaurantEachOfDayInCurrentMonthShouldReturnStats() {
        LocalDate firstDayOfMonth = mockedDate.with(TemporalAdjusters.firstDayOfMonth());

        // Create bills for different days in the month
        for (int i = 0; i < 8; i++) {
            LocalDate billDate = firstDayOfMonth.plusDays(i);
            LocalDateTime billDateTime = billDate.atTime(mockedTime);

            Customer monthCustomer = new Customer();
            monthCustomer.setName("Month Customer " + i);
            monthCustomer.setPhoneNumber("44444444" + i);
            monthCustomer.setAddress("Month Address " + i);
            monthCustomer.setRestaurant(restaurant);
            monthCustomer.setCurrentPoint(0);
            monthCustomer.setTotalPoint(0);
            monthCustomer.setDateCreated(billDateTime);
            monthCustomer = customerRepository.saveAndFlush(monthCustomer);

            // Create varying number of bills per day
            int billCount = (i % 3) + 1; // 1, 2, 3, 1, 2, 3, 1, 2
            for (int j = 0; j < billCount; j++) {
                Order monthOrder = new Order();
                monthOrder.setRestaurant(restaurant);
                monthOrder.setCustomer(monthCustomer);
                monthOrder.setEmployee(employee);
                monthOrder.setTableRestaurant(tableRestaurant);
                monthOrder.setOrderDate(billDate);
                monthOrder = orderRepository.saveAndFlush(monthOrder);

                Bill monthBill = new Bill();
                monthBill.setOrder(monthOrder);
                monthBill.setDateCreated(billDateTime);
                monthBill.setTotal(100.0 * (j + 1));
                monthBill.setPointUsed(0.0);
                billRepository.saveAndFlush(monthBill);
            }
        }

        List<StatisticTableResponse> result = statisticService
                .getDetailStatisticRestaurantEachOfDayInCurrentMonth(restaurantId);

        assertEquals(8, result.size());
        assertEquals(firstDayOfMonth, result.get(0).getTime());
        assertTrue(result.get(0).getNumbersBill() > 0);
        assertNotNull(result.get(0).getProfit());
    }

    // SS-6
    @Test
    void getDetailStatisticRestaurantEachOfDayInLastMonthShouldReturnStats() {
        LocalDateTime lastMonth = mockedDateTime.minusMonths(1);
        LocalDate firstDayOfLastMonth = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate();

        // Create bills for last month
        for (int i = 0; i < 8; i++) {
            LocalDate billDate = firstDayOfLastMonth.plusDays(i);
            LocalDateTime billDateTime = billDate.atTime(mockedTime);

            Customer lastMonthCustomer = new Customer();
            lastMonthCustomer.setName("Last Month Customer " + i);
            lastMonthCustomer.setPhoneNumber("55555555" + i);
            lastMonthCustomer.setAddress("Last Month Address " + i);
            lastMonthCustomer.setRestaurant(restaurant);
            lastMonthCustomer.setCurrentPoint(0);
            lastMonthCustomer.setTotalPoint(0);
            lastMonthCustomer.setDateCreated(billDateTime);
            lastMonthCustomer = customerRepository.saveAndFlush(lastMonthCustomer);

            Order lastMonthOrder = new Order();
            lastMonthOrder.setRestaurant(restaurant);
            lastMonthOrder.setCustomer(lastMonthCustomer);
            lastMonthOrder.setEmployee(employee);
            lastMonthOrder.setTableRestaurant(tableRestaurant);
            lastMonthOrder.setOrderDate(billDate);
            lastMonthOrder = orderRepository.saveAndFlush(lastMonthOrder);

            Bill lastMonthBill = new Bill();
            lastMonthBill.setOrder(lastMonthOrder);
            lastMonthBill.setDateCreated(billDateTime);
            lastMonthBill.setTotal(50.0);
            lastMonthBill.setPointUsed(0.0);
            billRepository.saveAndFlush(lastMonthBill);
        }

        List<StatisticTableResponse> result = statisticService
                .getDetailStatisticRestaurantEachOfDayInLastMonth(restaurantId);

        assertEquals(8, result.size());
        assertEquals(firstDayOfLastMonth, result.get(0).getTime());
        assertNotNull(result.get(0).getProfit());
    }

    // SS-7
    @Test
    void getValueByTimeAndCurrentDateForRestaurantShouldReturnHourlyStats() {
        // Create bills at different hours
        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime billDateTime = mockedDate.atTime(hour, 0);

            Customer hourCustomer = new Customer();
            hourCustomer.setName("Hour Customer " + hour);
            hourCustomer.setPhoneNumber("66666666" + String.format("%02d", hour));
            hourCustomer.setAddress("Hour Address " + hour);
            hourCustomer.setRestaurant(restaurant);
            hourCustomer.setCurrentPoint(0);
            hourCustomer.setTotalPoint(0);
            hourCustomer.setDateCreated(billDateTime);
            hourCustomer = customerRepository.saveAndFlush(hourCustomer);

            Order hourOrder = new Order();
            hourOrder.setRestaurant(restaurant);
            hourOrder.setCustomer(hourCustomer);
            hourOrder.setEmployee(employee);
            hourOrder.setTableRestaurant(tableRestaurant);
            hourOrder.setOrderDate(mockedDate);
            hourOrder = orderRepository.saveAndFlush(hourOrder);

            Bill hourBill = new Bill();
            hourBill.setOrder(hourOrder);
            hourBill.setDateCreated(billDateTime);
            hourBill.setTotal(100.0 + hour * 10);
            hourBill.setPointUsed(0.0);
            billRepository.saveAndFlush(hourBill);
        }

        List<StatisticChartValueManager> result = statisticService
                .getValueByTimeAndCurrentDateForRestaurant(restaurantId);

        assertEquals(24, result.size());
        assertEquals("00:00", result.get(0).getTime());
        assertNotNull(result.get(0).getValue());
    }

    // SS-8
    @Test
    void getDetailStatisticRestaurantEachOfDayInCurrentWeekShouldReturnStats() {
        LocalDate startOfWeek = mockedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Create bills for each day of the week
        for (int i = 0; i < 7; i++) {
            LocalDate billDate = startOfWeek.plusDays(i);
            LocalDateTime billDateTime = billDate.atTime(mockedTime);

            Customer weekCustomer = new Customer();
            weekCustomer.setName("Week Customer " + i);
            weekCustomer.setPhoneNumber("77777777" + i);
            weekCustomer.setAddress("Week Address " + i);
            weekCustomer.setRestaurant(restaurant);
            weekCustomer.setCurrentPoint(0);
            weekCustomer.setTotalPoint(0);
            weekCustomer.setDateCreated(billDateTime);
            weekCustomer = customerRepository.saveAndFlush(weekCustomer);

            // Create varying number of bills per day
            int billCount = (i % 3) + 1;
            for (int j = 0; j < billCount; j++) {
                Order weekOrder = new Order();
                weekOrder.setRestaurant(restaurant);
                weekOrder.setCustomer(weekCustomer);
                weekOrder.setEmployee(employee);
                weekOrder.setTableRestaurant(tableRestaurant);
                weekOrder.setOrderDate(billDate);
                weekOrder = orderRepository.saveAndFlush(weekOrder);

                Bill weekBill = new Bill();
                weekBill.setOrder(weekOrder);
                weekBill.setDateCreated(billDateTime);
                weekBill.setTotal(200.0 * (j + 1));
                weekBill.setPointUsed(0.0);
                billRepository.saveAndFlush(weekBill);
            }
        }

        List<StatisticTableResponse> result = statisticService
                .getDetailStatisticRestaurantEachOfDayInCurrentWeek(restaurantId);

        assertEquals(7, result.size());
        assertEquals(startOfWeek, result.get(0).getTime());
        assertTrue(result.get(0).getNumbersBill() > 0);
        assertNotNull(result.get(0).getProfit());
    }

    // SS-9
    @Test
    void getDetailStatisticRestaurantEachOfDayInLastWeekShouldReturnStats() {
        LocalDate startOfLastWeek = mockedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);

        // Create bills for each day of last week
        for (int i = 0; i < 7; i++) {
            LocalDate billDate = startOfLastWeek.plusDays(i);
            LocalDateTime billDateTime = billDate.atTime(mockedTime);

            Customer lastWeekCustomer = new Customer();
            lastWeekCustomer.setName("Last Week Customer " + i);
            lastWeekCustomer.setPhoneNumber("88888888" + i);
            lastWeekCustomer.setAddress("Last Week Address " + i);
            lastWeekCustomer.setRestaurant(restaurant);
            lastWeekCustomer.setCurrentPoint(0);
            lastWeekCustomer.setTotalPoint(0);
            lastWeekCustomer.setDateCreated(billDateTime);
            lastWeekCustomer = customerRepository.saveAndFlush(lastWeekCustomer);

            Order lastWeekOrder = new Order();
            lastWeekOrder.setRestaurant(restaurant);
            lastWeekOrder.setCustomer(lastWeekCustomer);
            lastWeekOrder.setEmployee(employee);
            lastWeekOrder.setTableRestaurant(tableRestaurant);
            lastWeekOrder.setOrderDate(billDate);
            lastWeekOrder = orderRepository.saveAndFlush(lastWeekOrder);

            Bill lastWeekBill = new Bill();
            lastWeekBill.setOrder(lastWeekOrder);
            lastWeekBill.setDateCreated(billDateTime);
            lastWeekBill.setTotal(150.0);
            lastWeekBill.setPointUsed(0.0);
            billRepository.saveAndFlush(lastWeekBill);
        }

        List<StatisticTableResponse> result = statisticService
                .getDetailStatisticRestaurantEachOfDayInLastWeek(restaurantId);

        assertEquals(7, result.size());
        assertEquals(startOfLastWeek, result.get(0).getTime());
        assertTrue(result.get(0).getNumbersBill() > 0);
        assertNotNull(result.get(0).getProfit());
    }
}
