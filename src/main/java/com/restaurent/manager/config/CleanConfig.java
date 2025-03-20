package com.restaurent.manager.config;

import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class CleanConfig {

    RestaurantRepository restaurantRepository;
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleFixedDelayTask() {
        log.info("Size : " + restaurantRepository.findByRestaurantPackageIdIsNotNullAndExpiryDateBefore(LocalDateTime.now()).size());
        List<Restaurant> restaurants = restaurantRepository.findByRestaurantPackageIdIsNotNullAndExpiryDateBefore(LocalDateTime.now());
        restaurants.forEach( restaurant ->
                restaurant.setRestaurantPackage(null));
    }
}
