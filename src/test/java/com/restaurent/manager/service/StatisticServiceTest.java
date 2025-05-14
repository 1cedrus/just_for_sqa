package com.restaurent.manager.service;

import com.restaurent.manager.dto.response.StatisticChartValueManager;
import com.restaurent.manager.dto.response.StatisticResponse;
import com.restaurent.manager.dto.response.StatisticTableResponse;
import com.restaurent.manager.entity.Bill;
import com.restaurent.manager.entity.Customer;
import com.restaurent.manager.repository.BillRepository;
import com.restaurent.manager.repository.CustomerRepository;
import com.restaurent.manager.service.impl.StatisticService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StatisticServiceTest {
    @Mock
    CustomerRepository customerRepository;

    @Mock
    BillRepository billRepository;

    @Mock
    IBillService billService;

    @Mock
    Clock clock;

    @InjectMocks
    StatisticService statisticService;

    Long restaurantId;

    LocalDate mockedDate;
    LocalTime mockedTime;
    LocalDateTime mockedDateTime;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        restaurantId = 1L;

        mockedDate = LocalDate.of(2025, 4, 8);
        mockedTime = LocalTime.of(12, 0);
        mockedDateTime = LocalDateTime.of(mockedDate, mockedTime);

        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
            fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    // SS-1
    @Test
    void getStatisticRestaurantByIdShouldReturnStatsForToday() {
        when(customerRepository.findCustomerByRestaurant_IdInToday(restaurantId)).thenReturn(List.of(new Customer(), new Customer()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(mockedDate))).thenReturn(List.of(new Bill(), new Bill()));
        when(billService.getProfitRestaurantByIdAndDate(restaurantId, mockedDateTime)).thenReturn(1000.0);
        when(billService.getVatValueForRestaurantCurrent(restaurantId, mockedDateTime)).thenReturn(200.0);

        StatisticResponse result = statisticService.getStatisticRestaurantById(restaurantId, "1");

        assertNotNull(result);
        assertEquals(2, result.getNumbersCustomer());
        assertEquals(2, result.getNumbersBill());
        assertEquals(1000.0, result.getProfit());
        assertEquals(200.0, result.getVat());

        verify(customerRepository).findCustomerByRestaurant_IdInToday(restaurantId);
        verify(billRepository).findByDateCreated(restaurantId, Date.valueOf(mockedDate));
        verify(billService).getProfitRestaurantByIdAndDate(restaurantId, mockedDateTime);
        verify(billService).getVatValueForRestaurantCurrent(restaurantId, mockedDateTime);
    }

    // SS-2
    @Test
    void getStatisticRestaurantByIdShouldReturnStatsForYesterday() {
        LocalDate yesterday = mockedDate.minusDays(1);
        when(customerRepository.findCustomerByRestaurant_IdInYesterday(restaurantId, Date.valueOf(yesterday))).thenReturn(List.of(new Customer()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(yesterday))).thenReturn(List.of(new Bill()));
        when(billService.getProfitRestaurantByIdAndDate(restaurantId, mockedDateTime.minusDays(1))).thenReturn(500.0);
        when(billService.getVatValueForRestaurantCurrent(restaurantId, mockedDateTime.minusDays(1))).thenReturn(100.0);

        // Act
        StatisticResponse result = statisticService.getStatisticRestaurantById(restaurantId, "-1");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNumbersCustomer());
        assertEquals(1, result.getNumbersBill());
        assertEquals(500.0, result.getProfit());
        assertEquals(100.0, result.getVat());

        verify(customerRepository).findCustomerByRestaurant_IdInYesterday(restaurantId, Date.valueOf(yesterday));
        verify(billRepository).findByDateCreated(restaurantId, Date.valueOf(yesterday));
        verify(billService).getProfitRestaurantByIdAndDate(restaurantId, mockedDateTime.minusDays(1));
        verify(billService).getVatValueForRestaurantCurrent(restaurantId, mockedDateTime.minusDays(1));
    }

    // SS-3
    @Test
    void getStatisticRestaurantByIdShouldReturnNullForInvalidDay() {
        // Act
        StatisticResponse result = statisticService.getStatisticRestaurantById(restaurantId, "0");

        // Assert
        assertNull(result);
    }

    // SS-4
    @Test
    void getStatisticByRestaurantIdBetweenStartDayToEndDayShouldReturnStats() {
        LocalDateTime start = mockedDateTime.minusDays(2);
        LocalDateTime end = mockedDateTime;

        when(customerRepository.findCustomerByRestaurantIdInStartDateAndEndDate(restaurantId, start, end)).thenReturn(List.of(new Customer(), new Customer()));
        when(billRepository.findByDateCreatedBetween(restaurantId, start, end)).thenReturn(List.of(new Bill(), new Bill(), new Bill()));
        when(billService.getProfitRestaurantByIdAndDateBetween(restaurantId, start, end)).thenReturn(1500.0);
        when(billService.getVatValueForRestaurantBetween(restaurantId, start, end)).thenReturn(300.0);

        // Act
        StatisticResponse result = statisticService.getStatisticByRestaurantIdBetweenStartDayToEndDay(restaurantId, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getNumbersCustomer());
        assertEquals(3, result.getNumbersBill());
        assertEquals(1500.0, result.getProfit());
        assertEquals(300.0, result.getVat());

        verify(customerRepository).findCustomerByRestaurantIdInStartDateAndEndDate(restaurantId, start, end);
        verify(billRepository).findByDateCreatedBetween(restaurantId, start, end);
        verify(billService).getProfitRestaurantByIdAndDateBetween(restaurantId, start, end);
        verify(billService).getVatValueForRestaurantBetween(restaurantId, start, end);
    }

    // SS-5
    @Test
    void getDetailStatisticRestaurantEachOfDayInCurrentMonthShouldReturnStats() {
        LocalDate firstDayOfMonth = mockedDate.with(TemporalAdjusters.firstDayOfMonth());
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(firstDayOfMonth))).thenReturn(List.of(new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(firstDayOfMonth.plusDays(1)))).thenReturn(List.of(new Bill(), new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(firstDayOfMonth.plusDays(2)))).thenReturn(Collections.emptyList());
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(firstDayOfMonth.plusDays(3)))).thenReturn(List.of(new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(firstDayOfMonth.plusDays(4)))).thenReturn(List.of(new Bill(), new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(firstDayOfMonth.plusDays(5)))).thenReturn(List.of(new Bill()));
        when(billService.getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class))).thenReturn(100.0);

        List<StatisticTableResponse> result = statisticService.getDetailStatisticRestaurantEachOfDayInCurrentMonth(restaurantId);

        assertEquals(8, result.size());
        assertEquals(firstDayOfMonth, result.get(0).getTime());
        assertEquals(1, result.get(0).getNumbersBill());
        assertEquals(100.0, result.get(0).getProfit());
        assertEquals(firstDayOfMonth.plusDays(1), result.get(1).getTime());
        assertEquals(2, result.get(1).getNumbersBill());

        verify(billRepository, times(8)).findByDateCreated(eq(restaurantId), any(Date.class));
        verify(billService, times(8)).getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class));
    }

    // SS-6
    @Test
    void getDetailStatisticRestaurantEachOfDayInLastMonthShouldReturnStats() {
        LocalDateTime lastMonth = mockedDateTime.minusMonths(1);
        LocalDate firstDayOfLastMonth = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate();

        when(billService.getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class))).thenReturn(50.0);

        List<StatisticTableResponse> result = statisticService.getDetailStatisticRestaurantEachOfDayInLastMonth(restaurantId);

        assertEquals(8, result.size()); // Days 1-8 of April 2025
        assertEquals(firstDayOfLastMonth, result.get(0).getTime());
        assertEquals(50.0, result.get(0).getProfit());

        verify(billService, times(8)).getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class));
    }

    // SS-7
    @Test
    void getValueByTimeAndCurrentDateForRestaurantShouldReturnHourlyStats() {
        when(billService.getTotalValueByTimeAndCurrentForRestaurant(eq(restaurantId), anyString(), anyString())).thenReturn(0.0);
        when(billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId, "23:00", "00:00")).thenReturn(100.0);
        when(billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId, "00:00", "01:00")).thenReturn(200.0);
        when(billService.getTotalValueByTimeAndCurrentForRestaurant(restaurantId, "01:00", "02:00")).thenReturn(300.0);

        List<StatisticChartValueManager> result = statisticService.getValueByTimeAndCurrentDateForRestaurant(restaurantId);

        assertEquals(24, result.size());
        assertEquals("00:00", result.get(0).getTime());
        assertEquals(100.0, result.get(0).getValue());
        assertEquals("01:00", result.get(1).getTime());
        assertEquals(200.0, result.get(1).getValue());
        assertEquals("02:00", result.get(2).getTime());
        assertEquals(300.0, result.get(2).getValue());

        verify(billService, times(24)).getTotalValueByTimeAndCurrentForRestaurant(eq(restaurantId), anyString(), anyString());
        verify(billService, times(1)).getTotalValueByTimeAndCurrentForRestaurant(eq(restaurantId), eq("23:00"), eq("00:00"));
        verify(billService, times(1)).getTotalValueByTimeAndCurrentForRestaurant(eq(restaurantId), eq("00:00"), eq("01:00"));
    }

    // SS-8
    @Test
    void getDetailStatisticRestaurantEachOfDayInCurrentWeekShouldReturnStats() {
        LocalDate startOfWeek = mockedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfWeek))).thenReturn(List.of(new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfWeek.plusDays(1)))).thenReturn(List.of(new Bill(), new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfWeek.plusDays(2)))).thenReturn(Collections.emptyList());
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfWeek.plusDays(3)))).thenReturn(List.of(new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfWeek.plusDays(4)))).thenReturn(List.of(new Bill(), new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfWeek.plusDays(5)))).thenReturn(List.of(new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfWeek.plusDays(6)))).thenReturn(List.of(new Bill(), new Bill(), new Bill()));
        when(billService.getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class))).thenReturn(200.0);

        List<StatisticTableResponse> result = statisticService.getDetailStatisticRestaurantEachOfDayInCurrentWeek(restaurantId);

        assertEquals(7, result.size()); // Monday to Sunday
        assertEquals(startOfWeek, result.get(0).getTime());
        assertEquals(1, result.get(0).getNumbersBill());
        assertEquals(200.0, result.get(0).getProfit());
        assertEquals(startOfWeek.plusDays(6), result.get(6).getTime());
        assertEquals(3, result.get(6).getNumbersBill());

        verify(billRepository, times(7)).findByDateCreated(eq(restaurantId), any(Date.class));
        verify(billService, times(7)).getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class));
    }

    // SS-9
    @Test
    void getDetailStatisticRestaurantEachOfDayInLastWeekShouldReturnStats() {
        LocalDate startOfLastWeek = mockedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1); // March 24, 2025
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfLastWeek))).thenReturn(List.of(new Bill()));
        when(billRepository.findByDateCreated(restaurantId, Date.valueOf(startOfLastWeek.plusDays(1)))).thenReturn(List.of(new Bill(), new Bill()));
        when(billService.getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class))).thenReturn(150.0);

        List<StatisticTableResponse> result = statisticService.getDetailStatisticRestaurantEachOfDayInLastWeek(restaurantId);

        assertEquals(7, result.size()); // Monday to Sunday
        assertEquals(startOfLastWeek, result.get(0).getTime());
        assertEquals(1, result.get(0).getNumbersBill());
        assertEquals(150.0, result.get(0).getProfit());

        verify(billRepository, times(7)).findByDateCreated(eq(restaurantId), any(Date.class));
        verify(billService, times(7)).getProfitRestaurantByIdAndDate(eq(restaurantId), any(LocalDateTime.class));
    }
}
