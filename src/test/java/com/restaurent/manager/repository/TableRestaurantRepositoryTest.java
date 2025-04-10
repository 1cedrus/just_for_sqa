package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Area;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.TableRestaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TableRestaurantRepositoryTest {
    @Autowired
    TableRestaurantRepository tableRestaurantRepository;

    @Autowired
    AreaRepository areaRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    Area area;
    Restaurant restaurant;
    TableRestaurant tableRestaurant;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant = restaurantRepository.saveAndFlush(restaurant);

        area = new Area();
        area.setName("Test Area");
        area.setRestaurant(restaurant);
        area = areaRepository.saveAndFlush(area);

        tableRestaurant = new TableRestaurant();
        tableRestaurant.setName("Test Table");
        tableRestaurant.setArea(area);
        tableRestaurant.setHidden(false);
        tableRestaurant = tableRestaurantRepository.saveAndFlush(tableRestaurant);

    }

    @Test
    void findTopByRestaurant_IdAndNameStartingWithOrderByNameDescShouldReturnLatestTableWhenMatching() {
        TableRestaurant result = tableRestaurantRepository.findTopByRestaurant_IdAndNameStartingWithOrderByNameDesc(restaurant.getId(), "Test");

        assertNotNull(result);
        assertEquals("Test Table", result.getName()); // Should return the latest by ID descending
        assertEquals(area.getId(), result.getArea().getId());
    }

    @Test
    void findTopByRestaurant_IdAndNameStartingWithOrderByNameDesc_ShouldReturnNull_WhenNoMatch() {
        TableRestaurant result = tableRestaurantRepository.findTopByRestaurant_IdAndNameStartingWithOrderByNameDesc(restaurant.getId(), "Another");

        assertNull(result);
    }
}
