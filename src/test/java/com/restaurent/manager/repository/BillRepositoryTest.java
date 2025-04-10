package com.restaurent.manager.repository;


import com.restaurent.manager.entity.Bill;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.entity.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BillRepositoryTest {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long restaurant1Id;
    private Long restaurant2Id;
    private Long restaurantId;
    private LocalDate today;
    private LocalDate yesterday;
    private LocalDateTime now;
    private LocalDateTime twoDaysAgo;
    private LocalDateTime yesterdayy;
    private LocalDateTime todayMorning;
    private LocalDateTime tomorrow;


    void setup() {
        // Restaurant 1
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setRestaurantName("A");
        restaurant1 = restaurantRepository.save(restaurant1);
        restaurant1Id = restaurant1.getId();

        // Restaurant 2
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setRestaurantName("B");
        restaurant2 = restaurantRepository.save(restaurant2);
        restaurant2Id = restaurant2.getId();

        // Tạo 5 đơn hàng và bill cho restaurant1
        for (int i = 1; i <= 5; i++) {
            Order order = new Order();
            order.setRestaurant(restaurant1);
            order = orderRepository.save(order);

            Bill bill = new Bill();
            bill.setOrder(order);
            bill.setDateCreated(LocalDateTime.now().minusDays(i));
            billRepository.save(bill);
        }

        // Tạo 1 đơn hàng và bill cho restaurant2
        Order order = new Order();
        order.setRestaurant(restaurant2);
        order = orderRepository.save(order);

        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setDateCreated(LocalDateTime.now());
        billRepository.save(bill);
    }

    void setup1() {
        today = LocalDate.now();
        yesterday = today.minusDays(1);

        // Tạo restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);
        restaurantId = restaurant.getId();

        // Tạo order + bill hôm nay
        Order orderToday1 = new Order();
        orderToday1.setRestaurant(restaurant);
        orderToday1 = orderRepository.save(orderToday1);

        Bill billToday1 = new Bill();
        billToday1.setOrder(orderToday1);
        billToday1.setDateCreated(LocalDateTime.of(today, LocalDateTime.now().toLocalTime()));
        billRepository.save(billToday1);

        // Tạo thêm 1 bill khác cùng ngày
        Order orderToday2 = new Order();
        orderToday2.setRestaurant(restaurant);
        orderToday2 = orderRepository.save(orderToday2);

        Bill billToday2 = new Bill();
        billToday2.setOrder(orderToday2);
        billToday2.setDateCreated(LocalDateTime.of(today, LocalDateTime.now().toLocalTime().minusHours(1)));
        billRepository.save(billToday2);

        // Tạo order + bill ngày hôm qua
        Order orderYesterday = new Order();
        orderYesterday.setRestaurant(restaurant);
        orderYesterday = orderRepository.save(orderYesterday);

        Bill billYesterday = new Bill();
        billYesterday.setOrder(orderYesterday);
        billYesterday.setDateCreated(LocalDateTime.of(yesterday, LocalDateTime.now().toLocalTime()));
        billRepository.save(billYesterday);
    }

    void setup2() {
        now = LocalDateTime.now();
        yesterdayy = now.minusDays(1);
        twoDaysAgo = now.minusDays(2);
        todayMorning = now.withHour(8).withMinute(0);
        tomorrow = now.plusDays(1);

        // Tạo nhà hàng
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);
        restaurantId = restaurant.getId();

        // Tạo 3 bills ở các thời điểm khác nhau
        billRepository.save(createBill(restaurant, twoDaysAgo.withHour(10))); // -2 ngày
        billRepository.save(createBill(restaurant, yesterdayy.withHour(14)));  // -1 ngày
        billRepository.save(createBill(restaurant, todayMorning));            // hôm nay
    }

    void setup3() {
        today = LocalDate.now();
        // Tạo restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);
        restaurantId = restaurant.getId();

        // Tạo bill hôm nay lúc 09:00
        billRepository.save(createBill(restaurant, LocalDateTime.of(today, LocalTime.of(9, 0))));
        // Tạo bill hôm nay lúc 12:00
        billRepository.save(createBill(restaurant, LocalDateTime.of(today, LocalTime.of(12, 0))));
        // Tạo bill hôm nay lúc 18:00
        billRepository.save(createBill(restaurant, LocalDateTime.of(today, LocalTime.of(18, 0))));
        // Tạo bill hôm qua lúc 10:00
        billRepository.save(createBill(restaurant, LocalDateTime.of(today.minusDays(1), LocalTime.of(10, 0))));
    }

    private Bill createBill(Restaurant restaurant, LocalDateTime dateTime) {
        Order order = new Order();
        order.setRestaurant(restaurant);
        order = orderRepository.save(order);

        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setDateCreated(dateTime);
        return billRepository.save(bill);
    }


    @Test
    @DisplayName("Should return 5 bills for existing restaurant ID")
    void testFindByRestaurantIdWithResults() {
        setup();
        List<Bill> bills = billRepository.findByOrder_Restaurant_Id(restaurant1Id, PageRequest.of(0, 10));
        assertThat(bills).hasSize(5);
    }

    @Test
    @DisplayName("Should return empty list for non-existent restaurant ID")
    void testFindByRestaurantIdWithNoResults() {
        setup();
        Long invalidId = 999L;
        List<Bill> bills = billRepository.findByOrder_Restaurant_Id(invalidId, PageRequest.of(0, 10));
        assertThat(bills).isEmpty();
    }

    @Test
    @DisplayName("Should return paginated results for restaurant bills")
    void testFindByRestaurantIdWithPagination() {
        setup();
        List<Bill> page1 = billRepository.findByOrder_Restaurant_Id(restaurant1Id, PageRequest.of(0, 2));
        List<Bill> page2 = billRepository.findByOrder_Restaurant_Id(restaurant1Id, PageRequest.of(1, 2));

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
    }



    @Test
    @DisplayName("Should return 2 bills created today for given restaurant")
    void testFindByDateCreatedWithMatchingBills() {
        setup1();
        List<Bill> bills = billRepository.findByDateCreated(restaurantId, Date.valueOf(today));
        assertThat(bills).hasSize(2);
    }

    @Test
    @DisplayName("Should return 1 bill created yesterday for given restaurant")
    void testFindByDateCreatedWithSingleBill() {
        setup1();
        List<Bill> bills = billRepository.findByDateCreated(restaurantId, Date.valueOf(yesterday));
        assertThat(bills).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty list when no bills created on selected date")
    void testFindByDateCreatedWithNoBillOnDate() {
        setup1();
        LocalDate future = today.plusDays(3);
        List<Bill> bills = billRepository.findByDateCreated(restaurantId, Date.valueOf(future));
        assertThat(bills).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for non-existent restaurant ID")
    void testFindByDateCreatedWithInvalidRestaurantId() {
        setup1();
        List<Bill> bills = billRepository.findByDateCreated(999L, Date.valueOf(today));
        assertThat(bills).isEmpty();
    }

    @Test
    @DisplayName("Should return all bills in full range (2 days ago to tomorrow)")
    void testFindByDateCreatedBetweenWithAllBills() {
        setup2();
        List<Bill> bills = billRepository.findByDateCreatedBetween(
                restaurantId,
                twoDaysAgo.withHour(0),
                tomorrow.withHour(23)
        );
        assertThat(bills).hasSize(3);
    }

    @Test
    @DisplayName("Should return 1 bill for yesterday only")
    void testFindByDateCreatedBetweenWithOneDayRange() {
        setup2();
        List<Bill> bills = billRepository.findByDateCreatedBetween(
                restaurantId,
                yesterdayy.withHour(0),
                yesterdayy.withHour(23)
        );
        assertThat(bills).hasSize(1);
    }

    @Test
    @DisplayName("Should include bills exactly at start or end time")
    void testFindByDateCreatedBetweenInclusiveBoundaries() {
        setup2();
        List<Bill> bills = billRepository.findByDateCreatedBetween(
                restaurantId,
                todayMorning,
                todayMorning
        );
        assertThat(bills).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty list when no bills match in range")
    void testFindByDateCreatedBetweenNoResults() {
        setup2();
        List<Bill> bills = billRepository.findByDateCreatedBetween(
                restaurantId,
                now.plusDays(5),
                now.plusDays(6)
        );
        assertThat(bills).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for invalid restaurant ID")
    void testFindByDateCreatedBetweenInvalidRestaurant() {
        setup2();
        List<Bill> bills = billRepository.findByDateCreatedBetween(
                999L,
                twoDaysAgo,
                tomorrow
        );
        assertThat(bills).isEmpty();
    }





    @Test
    @DisplayName("Should return bills between 08:00 and 13:00 on current date")
    void testBillsWithinTimeRange() {
        setup3();
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentDate(
                restaurantId,
                "08:00:00",
                "13:00:00"
        );
        assertThat(bills).hasSize(2); // 09:00 and 12:00
    }

    @Test
    @DisplayName("Should return only one bill exactly at start time")
    void testBillAtStartTime() {
        setup3();
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentDate(
                restaurantId,
                "09:00:00",
                "09:00:00"
        );
        assertThat(bills).hasSize(1);
    }

    @Test
    @DisplayName("Should return only one bill exactly at end time")
    void testBillAtEndTime() {
        setup3();
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentDate(
                restaurantId,
                "18:00:00",
                "18:00:00"
        );
        assertThat(bills).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty when time range does not match any bill today")
    void testNoBillsInTimeRangeToday() {
        setup3();
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentDate(
                restaurantId,
                "00:00:00",
                "08:00:00"
        );
        assertThat(bills).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when there is no bill for today")
    void testNoBillsToday() {
        setup3();
        // clear all today's bills
        billRepository.deleteAll();

        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentDate(
                restaurantId,
                "08:00:00",
                "20:00:00"
        );
        assertThat(bills).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when restaurant does not exist")
    void testInvalidRestaurantId() {
        setup3();
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentDate(
                999L,
                "08:00:00",
                "20:00:00"
        );
        assertThat(bills).isEmpty();
    }
}
