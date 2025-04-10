package com.restaurent.manager.repository;


import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Schedule;
import com.restaurent.manager.entity.TableRestaurant;
import com.restaurent.manager.enums.SCHEDULE_STATUS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScheduleRepositoryTest {
    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    TableRestaurantRepository tableRestaurantRepository;

    TableRestaurant tableRestaurant;
    Restaurant restaurant;
    Schedule schedule;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant = restaurantRepository.saveAndFlush(restaurant);

        tableRestaurant = new TableRestaurant();
        tableRestaurant.setName("Table1");
        tableRestaurant = tableRestaurantRepository.saveAndFlush(tableRestaurant);

        schedule = new Schedule();
        schedule.setCustomerName("John Doe");
        schedule.setCustomerPhone("1234567890");
        schedule.setBookedDate(LocalDate.of(2025, 4, 8));
        schedule.setTime(LocalTime.of(12, 0));
        schedule.setIntendTime(LocalTime.of(13, 0));
        schedule.setNumbersOfCustomer(4);
        schedule.setTableRestaurants(Set.of(tableRestaurant));
        schedule.setRestaurant(restaurant);
        schedule.setStatus(SCHEDULE_STATUS.PENDING);
        schedule = scheduleRepository.saveAndFlush(schedule);
    }

    @Test
    void findSchedulesByTableAndDateRangeShouldReturnSchedulesWhenMatchingCriteria() {
        LocalDate bookedDate = LocalDate.of(2025, 4, 8);
        LocalTime startTime = LocalTime.of(12, 30);
        LocalTime endTime = LocalTime.of(13, 30);

        List<Schedule> result = scheduleRepository.findSchedulesByTableAndDateRange(
            tableRestaurant.getId(), bookedDate, startTime, endTime);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(schedule.getId(), result.get(0).getId());
        assertEquals(SCHEDULE_STATUS.PENDING, result.get(0).getStatus());
    }

    @Test
    void findSchedulesByTableAndDateRangeShouldReturnEmptyListWhenNoMatches() {
        Long tableRestaurantId = 1L;
        LocalDate bookedDate = LocalDate.of(2025, 4, 9);
        LocalTime startTime = LocalTime.of(12, 0);
        LocalTime endTime = LocalTime.of(13, 0);

        List<Schedule> result = scheduleRepository.findSchedulesByTableAndDateRange(
            tableRestaurantId, bookedDate, startTime, endTime);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findSchedulesByTableIdAndDateShouldReturnSchedulesWhenMatchingCriteria() {
        LocalDate bookedDate = LocalDate.of(2025, 4, 8);
        Pageable pageable = PageRequest.of(0, 10);

        List<Schedule> result = scheduleRepository.findSchedulesByTableIdAndDate(tableRestaurant.getId(), bookedDate, pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(schedule.getId(), result.get(0).getId());
    }

    @Test
    void findSchedulesByTableIdAndDateShouldReturnEmptyListWhenNoMatches() {
        LocalDate bookedDate = LocalDate.of(2025, 4, 9);
        Pageable pageable = PageRequest.of(0, 10);

        List<Schedule> result = scheduleRepository.findSchedulesByTableIdAndDate(tableRestaurant.getId(), bookedDate, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void countSchedulesByTableIdAndDateShouldReturnCountWhenMatchingCriteria() {
        LocalDate bookedDate = LocalDate.of(2025, 4, 8);

        int count = scheduleRepository.countSchedulesByTableIdAndDate(tableRestaurant.getId(), bookedDate);

        assertEquals(1, count);
    }

    @Test
    void countSchedulesByTableIdAndDateShouldReturnZeroWhenNoMatches() {
        LocalDate bookedDate = LocalDate.of(2025, 4, 9); // Different date

        int count = scheduleRepository.countSchedulesByTableIdAndDate(tableRestaurant.getId(), bookedDate);

        assertEquals(0, count);
    }

    @Test
    void findByTableIdAndBookedDateShouldReturnSchedulesWhenMatchingCriteria() {
        LocalDate bookedDate = LocalDate.of(2025, 4, 8);

        List<Schedule> result = scheduleRepository.findByTableIdAndBookedDate(tableRestaurant.getId(), bookedDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(schedule.getId(), result.get(0).getId());
        assertEquals(SCHEDULE_STATUS.PENDING, result.get(0).getStatus());
    }

    @Test
    void findByTableIdAndBookedDateShouldReturnEmptyListWhenNoMatches() {
        LocalDate bookedDate = LocalDate.of(2025, 4, 9); // Different date

        List<Schedule> result = scheduleRepository.findByTableIdAndBookedDate(tableRestaurant.getId(), bookedDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
