package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Package;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.RestaurantPackagePaymentHistory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class RestaurantPackagePaymentHistoryRepositoryTest {

    @Autowired
    RestaurantPackagePaymentHistoryRepository restaurantPackagePaymentHistoryRepository;
    @Autowired
    RestaurantRepository restaurantRepository;
    @Autowired
    PackageRepository packageRepository;

    private Restaurant restaurant;
    private Package package1;

    @BeforeEach
    void setup() {
        restaurant = new Restaurant();
        restaurantRepository.saveAndFlush(restaurant);
        package1 = new Package();
        packageRepository.saveAndFlush(package1);
    }

    @Test
    void testChuan1_findByDateCreated() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

        RestaurantPackagePaymentHistory restaurantPackagePaymentHistory1 = RestaurantPackagePaymentHistory.builder()
                .id(1L)
                .dateCreated(LocalDateTime.parse("00:00:00 06/04/2025", dateTimeFormatter))
                .restaurantId(restaurant.getId())
                .packageId(package1.getId())
                .months(12)
                .totalMoney(100D)
                .isPaid(true)
                .build();

        RestaurantPackagePaymentHistory restaurantPackagePaymentHistory2 = RestaurantPackagePaymentHistory.builder()
                .id(2L)
                .dateCreated(LocalDateTime.parse("00:00:00 05/04/2025", dateTimeFormatter))
                .restaurantId(restaurant.getId())
                .packageId(package1.getId())
                .months(1)
                .totalMoney(99D)
                .isPaid(false)
                .build();

        RestaurantPackagePaymentHistory restaurantPackagePaymentHistory3 = RestaurantPackagePaymentHistory.builder()
                .id(3L)
                .dateCreated(LocalDateTime.parse("20:00:00 06/04/2025", dateTimeFormatter))
                .restaurantId(restaurant.getId())
                .packageId(package1.getId())
                .months(5)
                .totalMoney(101D)
                .isPaid(true)
                .build();

        restaurantPackagePaymentHistoryRepository.saveAllAndFlush(List.of(
                restaurantPackagePaymentHistory1,
                restaurantPackagePaymentHistory2,
                restaurantPackagePaymentHistory3
        ));

        // Dùng LocalDate thay vì Date
        List<RestaurantPackagePaymentHistory> list = restaurantPackagePaymentHistoryRepository.findByDateCreated(
                Date.valueOf(LocalDate.parse("06/04/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        );

        Assertions.assertThat(list.size()).isEqualTo(2);
        Assertions.assertThat(list).extracting(RestaurantPackagePaymentHistory::getTotalMoney).containsExactlyInAnyOrder(100D, 101D);
        Assertions.assertThat(list).extracting(RestaurantPackagePaymentHistory::getMonths).containsExactlyInAnyOrder(12, 5);
        Assertions.assertThat(list).extracting(RestaurantPackagePaymentHistory::getId).containsExactlyInAnyOrder(1L, 3L);
        list.forEach(e -> {
            Assertions.assertThat(e.isPaid()).isTrue();
            Assertions.assertThat(e.getDateCreated().toLocalDate()).isEqualTo(LocalDate.parse("06/04/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
    }
}