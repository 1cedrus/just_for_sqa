package com.restaurent.manager.repository;


import com.restaurent.manager.entity.Area;
import com.restaurent.manager.entity.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AreaRepositoryTest {

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Returns Area list by restaurantId - with data")
    void testFindByRestaurantId_WithExistingAreas() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        Area area1 = new Area();
        area1.setName("Khu A");
        area1.setRestaurant(restaurant);
        entityManager.persist(area1);

        Area area2 = new Area();
        area2.setName("Khu B");
        area2.setRestaurant(restaurant);
        entityManager.persist(area2);

        entityManager.flush();

        List<Area> result = areaRepository.findByRestaurant_Id(restaurant.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Area::getName).containsExactlyInAnyOrder("Khu A", "Khu B");
    }

    @Test
    @DisplayName("Returns an empty Area list when the restaurant has no Areas.")
    void testFindByRestaurantId_WithNoAreas() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Empty Restaurant");
        entityManager.persistAndFlush(restaurant);

        List<Area> result = areaRepository.findByRestaurant_Id(restaurant.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Returns empty list when restaurantId does not exist")
    void testFindByRestaurantId_NonExistentRestaurantId() {
        List<Area> result = areaRepository.findByRestaurant_Id(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Count the number of Areas when the restaurant has many Areas")
    void testCountByRestaurantId_WithMultipleAreas() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Restaurant A");
        entityManager.persist(restaurant);

        Area area1 = new Area();
        area1.setName("Area 1");
        area1.setRestaurant(restaurant);
        entityManager.persist(area1);

        Area area2 = new Area();
        area2.setName("Area 2");
        area2.setRestaurant(restaurant);
        entityManager.persist(area2);

        entityManager.flush();

        int count = areaRepository.countByRestaurant_Id(restaurant.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Count Areas when restaurant has no Areas")
    void testCountByRestaurantId_WithNoAreas() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Empty Restaurant");
        entityManager.persistAndFlush(restaurant);

        int count = areaRepository.countByRestaurant_Id(restaurant.getId());
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Count Area with non-existent restaurantId")
    void testCountByRestaurantId_WithNonExistingRestaurantId() {
        int count = areaRepository.countByRestaurant_Id(999L);
        assertThat(count).isEqualTo(0);
    }

}
