package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho RoleRepository
 * Sử dụng H2 in-memory database để thao tác trực tiếp với database
 * Mục tiêu: Đạt branch coverage khoảng 80%
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Quản lý entities trong test

    @Autowired
    private RoleRepository roleRepository; // Repository cần test

    /**
     * Thiết lập dữ liệu trước mỗi test
     * Xóa database và thêm dữ liệu mẫu
     */
    @BeforeEach
    void setUp() {
        // Thêm role mẫu vào database
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrator role")
                .build();
        roleRepository.save(adminRole);
        roleRepository.flush();
    }

    // --- Tests cho existsByName ---
    /**
     * Test kiểm tra sự tồn tại của role theo tên - role tồn tại
     */
    @Test
    void testChuan1_ExistsByName() {
        // Kiểm tra role đã tồn tại trong database
        boolean exists = roleRepository.existsByName("ADMIN");

        // Kết quả phải là true vì role "ADMIN" đã được thêm trong setUp
        assertTrue(exists);
    }

    /**
     * Test kiểm tra sự tồn tại của role theo tên - role không tồn tại
     */
    @Test
    void testNgoaiLe1_ExistsByNam() {
        // Kiểm tra một role không tồn tại
        boolean exists = roleRepository.existsByName("USER");

        // Kết quả phải là false vì "USER" chưa được thêm
        assertFalse(exists);
    }

    /**
     * Test kiểm tra sự tồn tại với tên null
     */
    @Test
    void testNgoaiLe2_ExistsByName() {
        // Kiểm tra với input null
        boolean exists = roleRepository.existsByName(null);

        // Mong đợi false vì tên null không hợp lệ
        assertFalse(exists);
    }

    // --- Tests cho findByName ---
    /**
     * Test tìm role theo tên - tìm thấy
     */
    @Test
    void testChuan1_FindByName() {
        // Tìm role "ADMIN" đã thêm trong setUp
        Optional<Role> result = roleRepository.findByName("ADMIN");

        // Kiểm tra kết quả
        assertTrue(result.isPresent()); // Phải có giá trị
        assertEquals("ADMIN", result.get().getName()); // Tên phải khớp
        assertEquals("Administrator role", result.get().getDescription()); // Mô tả phải khớp
    }

    /**
     * Test tìm role theo tên - không tìm thấy
     */
    @Test
    void testNgoaiLe1_FindByName() {
        // Tìm một role không tồn tại
        Optional<Role> result = roleRepository.findByName("USER");

        // Kết quả phải là Optional.empty()
        assertFalse(result.isPresent());
    }

    /**
     * Test tìm role với tên null
     */
    @Test
    void testNgoaiLe2_FindByName() {
        // Tìm với tên null
        Optional<Role> result = roleRepository.findByName(null);

        // Mong đợi không tìm thấy (Optional.empty)
        assertFalse(result.isPresent());
    }

    /**
     * Test tìm role với tên rỗng
     */
    @Test
    void testChuan2_FindByName() {
        // Thêm một role với tên rỗng để test trường hợp đặc biệt
        Role emptyNameRole = Role.builder()
                .name("")
                .description("Empty name role")
                .build();
        entityManager.persist(emptyNameRole);
        entityManager.flush();

        // Tìm role với tên rỗng
        Optional<Role> result = roleRepository.findByName("");

        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals("", result.get().getName());
    }
}
