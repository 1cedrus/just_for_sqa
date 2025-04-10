package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Order;
import com.restaurent.manager.entity.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Order sampleOrder;
    private Restaurant sampleRestaurant;

    @BeforeEach
    public void setUp() {
        // Tạo dữ liệu mẫu
        Restaurant setupRestaurant = new Restaurant();

        Order setupOrder = new Order();
        setupOrder.setRestaurant(setupRestaurant);

        // Persist dữ liệu vào H2 database
        sampleRestaurant = entityManager.persist(setupRestaurant);
        sampleOrder = entityManager.persist(setupOrder);
        entityManager.flush();
    }

    // Test findOrderByRestaurant_Id - Thành công
    @Test
    public void testFindOrderByRestaurantId_Success() {
        List<Order> result = orderRepository.findOrderByRestaurant_Id(sampleRestaurant.getId());
        assertEquals(1, result.size(), "Phải tìm thấy 1 order với restaurantId " + sampleRestaurant.getId());
        assertEquals(sampleRestaurant.getId(), result.get(0).getRestaurant().getId(), "Restaurant ID phải khớp");
    }

    // Test findOrderByRestaurant_Id - Thất bại
    @Test
    public void testFindOrderByRestaurantId_NoMatch() {
        List<Order> result = orderRepository.findOrderByRestaurant_Id(999L);
        assertTrue(result.isEmpty(), "Không được tìm thấy order nào với restaurantId 999");
    }
}
