package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.RoleRequest;
import com.restaurent.manager.dto.response.RoleResponse;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.RoleRepository;
import com.restaurent.manager.service.impl.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho RoleService
 * Sử dụng database thật với profile test
 * Mục tiêu: Đạt branch coverage khoảng 80% cho tất cả các phương thức
 * Các test tập trung vào kiểm tra logic chính và các nhánh quan trọng
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        roleRepository.deleteAll();
    }

    // --- Kiểm thử cho createRole ---

    // Kiểm tra trường hợp vai trò đã tồn tại
    // ID: RoS-2
    @Test
    void testCreateRole_RoleExists() {
        // Chuẩn bị dữ liệu - tạo role đã tồn tại
        Role existingRole = createRole("ADMIN");
        roleRepository.save(existingRole);

        RoleRequest request = createRoleRequest("ADMIN");

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                roleService.createRole(request)
        );
        assertEquals(ErrorCode.ROLE_EXISTED, exception.getErrorCode());
        
        // Verify chỉ có 1 role trong database (role đã tồn tại)
        assertEquals(1, roleRepository.count());
    }

    // ID: RoS-1
    // Kiểm tra trường hợp vai trò chưa tồn tại
    @Test
    void testCreateRole_RoleNotExists() {
        // Chuẩn bị dữ liệu
        RoleRequest request = createRoleRequest("ADMIN");

        // Thực thi
        RoleResponse result = roleService.createRole(request);

        // Kiểm tra
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        
        // Verify trong database
        assertTrue(roleRepository.existsByName("ADMIN"));
        assertEquals(1, roleRepository.count());
    }

    // --- Kiểm thử cho getRoles ---

    // ID: RoS-3
    // Kiểm tra khi danh sách vai trò không rỗng
    @Test
    void testGetRoles_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Role role1 = createRole("ADMIN");
        Role role2 = createRole("USER");
        roleRepository.save(role1);
        roleRepository.save(role2);

        // Thực thi
        List<RoleResponse> result = roleService.getRoles();

        // Kiểm tra
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> "ADMIN".equals(r.getName())));
        assertTrue(result.stream().anyMatch(r -> "USER".equals(r.getName())));
    }

    // ID: RoS-4
    // Kiểm tra khi danh sách vai trò rỗng
    @Test
    void testGetRoles_EmptyList() {
        // Thực thi (database đã được clean trong setUp)
        List<RoleResponse> result = roleService.getRoles();

        // Kiểm tra
        assertTrue(result.isEmpty());
    }

    // --- Kiểm thử cho getRolesInRestaurant ---

    // ID: RoS-5
    // Kiểm tra khi tất cả vai trò trong nhà hàng tồn tại
    @Test
    void testGetRolesInRestaurant_AllRolesExist() {
        // Chuẩn bị dữ liệu - tạo các role cần thiết cho restaurant
        Role chef = createRole("CHEF");
        Role waiter = createRole("WAITER");
        Role hostess = createRole("HOSTESS");
        
        roleRepository.save(chef);
        roleRepository.save(waiter);
        roleRepository.save(hostess);

        // Thực thi
        List<RoleResponse> result = roleService.getRolesInRestaurant();

        // Kiểm tra
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r -> "CHEF".equals(r.getName())));
        assertTrue(result.stream().anyMatch(r -> "WAITER".equals(r.getName())));
        assertTrue(result.stream().anyMatch(r -> "HOSTESS".equals(r.getName())));
    }

    // ID: RoS-6
    // Kiểm tra khi một vai trò không tồn tại
    @Test
    void testGetRolesInRestaurant_RoleNotExist() {
        // Chuẩn bị dữ liệu - chỉ tạo 2 trong 3 role cần thiết (thiếu CHEF)
        Role waiter = createRole("WAITER");
        Role hostess = createRole("HOSTESS");
        
        roleRepository.save(waiter);
        roleRepository.save(hostess);
        // Không tạo CHEF role

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                roleService.getRolesInRestaurant()
        );
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
    }

    // --- Kiểm thử cho findByRoleName ---

    // ID: RoS-7
    // Kiểm tra khi vai trò tồn tại
    @Test
    void testFindByRoleName_RoleExists() {
        // Chuẩn bị dữ liệu
        String name = "ADMIN";
        Role role = createRole("ADMIN");
        roleRepository.save(role);

        // Thực thi
        Role result = roleService.findByRoleName(name);

        // Kiểm tra
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
    }

    // ID: RoS-8
    // Kiểm tra khi vai trò không tồn tại
    @Test
    void testFindByRoleName_RoleNotExists() {
        // Chuẩn bị dữ liệu - không tạo role nào

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                roleService.findByRoleName("ADMIN")
        );

        // Xác minh ngoại lệ đúng
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
    }

    // Helper methods để tạo đối tượng test
    private RoleRequest createRoleRequest(String name) {
        return RoleRequest.builder()
                .name(name)
                .build();
    }

    private Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}