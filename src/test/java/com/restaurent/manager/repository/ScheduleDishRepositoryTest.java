package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Combo;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.Schedule;
import com.restaurent.manager.entity.ScheduleDish;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho ScheduleDishRepository
 * Mục tiêu: Đạt branch coverage trên 80%
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class ScheduleDishRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Quản lý entities trong test

    @Autowired
    private ScheduleDishRepository scheduleDishRepository; // Repository cần test

    private Schedule schedule1;
    private Schedule schedule2;

    /**
     * Thiết lập dữ liệu trước mỗi test
     * Xóa database và thêm dữ liệu mẫu
     */
    @BeforeEach
    void setUp() {
        // Xóa toàn bộ dữ liệu trước mỗi test
        entityManager.getEntityManager()
                .createQuery("DELETE FROM ScheduleDish")
                .executeUpdate();
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Schedule")
                .executeUpdate();
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Dish")
                .executeUpdate();
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Combo")
                .executeUpdate();

        // Tạo dữ liệu mẫu
        // Schedule 1
        schedule1 = Schedule.builder()
                .build();
        entityManager.persist(schedule1);

        // Schedule 2
        schedule2 = Schedule.builder()
                .build();
        entityManager.persist(schedule2);

        // Dish
        Dish dish = Dish.builder()
                .name("Dish 1")
                .build();
        entityManager.persist(dish);

        // Combo
        Combo combo = Combo.builder()
                .name("Combo 1")
                .build();
        entityManager.persist(combo);

        // ScheduleDish cho schedule1
        ScheduleDish scheduleDish1 = ScheduleDish.builder()
                .schedule(schedule1)
                .dish(dish)
                .quantity(2)
                .build();
        ScheduleDish scheduleDish2 = ScheduleDish.builder()
                .schedule(schedule1)
                .combo(combo)
                .quantity(1)
                .build();
        entityManager.persist(scheduleDish1);
        entityManager.persist(scheduleDish2);

        // ScheduleDish cho schedule2
        ScheduleDish scheduleDish3 = ScheduleDish.builder()
                .schedule(schedule2)
                .dish(dish)
                .quantity(3)
                .build();
        entityManager.persist(scheduleDish3);

        entityManager.flush(); // Đảm bảo dữ liệu được ghi vào database
    }

    /**
     * Test tìm ScheduleDish theo schedule ID - có nhiều kết quả
     */
    @Test
    void testChuan1_FindBySchedule_Id() {
        // Tìm các ScheduleDish thuộc schedule1 (có 2 bản ghi)
        List<ScheduleDish> result = scheduleDishRepository.findBySchedule_Id(schedule1.getId());

        // Kiểm tra kết quả
        assertNotNull(result);           // Kết quả không được null
        assertEquals(2, result.size());  // Phải có 2 bản ghi
        assertTrue(result.stream().allMatch(sd -> sd.getSchedule().getId().equals(schedule1.getId()))); // Tất cả đều thuộc schedule1
    }

    /**
     * Test tìm ScheduleDish theo schedule ID - có một kết quả
     */
    @Test
    void testChuan2_FindBySchedule_Id() {
        // Tìm các ScheduleDish thuộc schedule2 (có 1 bản ghi)
        List<ScheduleDish> result = scheduleDishRepository.findBySchedule_Id(schedule2.getId());

        // Kiểm tra kết quả
        assertNotNull(result);           // Kết quả không được null
        assertEquals(1, result.size());  // Phải có 1 bản ghi
        assertEquals(schedule2.getId(), result.get(0).getSchedule().getId()); // Thuộc schedule2
    }

    /**
     * Test tìm ScheduleDish theo schedule ID - không có kết quả
     */
    @Test
    void testNgoaiLe1_FindBySchedule_Id() {
        // Tìm với một schedule ID không tồn tại
        List<ScheduleDish> result = scheduleDishRepository.findBySchedule_Id(999L);

        // Kiểm tra kết quả
        assertNotNull(result);      // Kết quả không được null
        assertTrue(result.isEmpty()); // Danh sách phải rỗng
    }

    /**
     * Test tìm ScheduleDish với schedule ID là null
     */
    @Test
    void testNgoaiLe2_FindBySchedule_Id() {
        // Tìm với schedule ID là null
        List<ScheduleDish> result = scheduleDishRepository.findBySchedule_Id(null);

        // Kiểm tra kết quả
        assertNotNull(result);      // Kết quả không được null
        assertTrue(result.isEmpty()); // Danh sách phải rỗng vì null không hợp lệ
    }
}
