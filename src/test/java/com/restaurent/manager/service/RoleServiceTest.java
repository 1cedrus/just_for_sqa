package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.RoleRequest;
import com.restaurent.manager.dto.response.RoleResponse;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.RoleMapper;
import com.restaurent.manager.repository.RoleRepository;
import com.restaurent.manager.service.impl.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Sử dụng MockitoExtension để tích hợp Mockito với JUnit 5
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    // Mô phỏng RoleRepository để giả lập các thao tác với cơ sở dữ liệu
    @Mock
    private RoleRepository roleRepository;

    // Mô phỏng RoleMapper để giả lập việc ánh xạ giữa các đối tượng
    @Mock
    private RoleMapper roleMapper;

    // InjectMocks tạo instance RoleService và tiêm các mock vào
    @InjectMocks
    private RoleService roleService;

    // Chạy trước mỗi test để đảm bảo trạng thái sạch sẽ
    @BeforeEach
    void setup() {
        // Không cần khởi tạo thủ công vì @InjectMocks tự xử lý
    }

    // --- Kiểm thử cho createRole ---

    // Kiểm tra trường hợp vai trò đã tồn tại
    // ID: RoS-2
    @Test
    void testCreateRole_RoleExists() {
        // Chuẩn bị dữ liệu
        RoleRequest request = createRoleRequest("ADMIN");
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                roleService.createRole(request)
        );
        assertEquals(ErrorCode.ROLE_EXISTED, exception.getErrorCode());
        // Xác minh chỉ gọi existsByName, không gọi save
        verify(roleRepository, times(1)).existsByName("ADMIN");
        verify(roleRepository, never()).save(any());
    }

    // ID: RoS-1
    // Kiểm tra trường hợp vai trò chưa tồn tại
    @Test
    void testCreateRole_RoleNotExists() {
        // Chuẩn bị dữ liệu
        RoleRequest request = createRoleRequest("ADMIN");
        Role role = createRole("ADMIN");
        RoleResponse response = createRoleResponse("ADMIN");

        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleMapper.toRole(request)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toRoleResponse(role)).thenReturn(response);

        // Thực thi
        RoleResponse result = roleService.createRole(request);

        // Kiểm tra
        verify(roleRepository, times(1)).existsByName("ADMIN");
        verify(roleMapper, times(1)).toRole(request);
        verify(roleRepository, times(1)).save(role);
        verify(roleMapper, times(1)).toRoleResponse(role);
        assertEquals("ADMIN", result.getName());
    }

    // --- Kiểm thử cho getRoles ---

    // ID: RoS-3
    // Kiểm tra khi danh sách vai trò không rỗng
    @Test
    void testGetRoles_NonEmptyList() {
        // Chuẩn bị dữ liệu
        Role role1 = createRole("ADMIN");
        Role role2 = createRole("USER");
        List<Role> roles = Arrays.asList(role1, role2);
        RoleResponse response1 = createRoleResponse("ADMIN");
        RoleResponse response2 = createRoleResponse("USER");

        when(roleRepository.findAll()).thenReturn(roles);
        when(roleMapper.toRoleResponse(role1)).thenReturn(response1);
        when(roleMapper.toRoleResponse(role2)).thenReturn(response2);

        // Thực thi
        List<RoleResponse> result = roleService.getRoles();

        // Kiểm tra
        verify(roleRepository, times(1)).findAll();
        verify(roleMapper, times(1)).toRoleResponse(role1);
        verify(roleMapper, times(1)).toRoleResponse(role2);
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
    }

    // ID: RoS-4
    // Kiểm tra khi danh sách vai trò rỗng
    @Test
    void testGetRoles_EmptyList() {
        // Chuẩn bị dữ liệu
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());

        // Thực thi
        List<RoleResponse> result = roleService.getRoles();

        // Kiểm tra
        verify(roleRepository, times(1)).findAll();
        verify(roleMapper, never()).toRoleResponse(any());
        assertTrue(result.isEmpty());
    }

    // --- Kiểm thử cho getRolesInRestaurant ---

    // ID: RoS-5
    // Kiểm tra khi tất cả vai trò trong nhà hàng tồn tại
    @Test
    void testGetRolesInRestaurant_AllRolesExist() {
        // Chuẩn bị dữ liệu
        RoleResponse chef = createRoleResponse("CHEF");
        RoleResponse waiter = createRoleResponse("WAITER");
        RoleResponse hostess = createRoleResponse("HOSTESS");

        when(roleRepository.findByName("CHEF")).thenReturn(Optional.of(createRole("CHEF")));
        when(roleRepository.findByName("WAITER")).thenReturn(Optional.of(createRole("WAITER")));
        when(roleRepository.findByName("HOSTESS")).thenReturn(Optional.of(createRole("HOSTESS")));
        when(roleMapper.toRoleResponse(any())).thenReturn(chef, waiter, hostess);

        // Thực thi
        List<RoleResponse> result = roleService.getRolesInRestaurant();

        // Kiểm tra
        verify(roleRepository, times(1)).findByName("CHEF");
        verify(roleRepository, times(1)).findByName("WAITER");
        verify(roleRepository, times(1)).findByName("HOSTESS");
        verify(roleMapper, times(3)).toRoleResponse(any());
        assertEquals(3, result.size());
        assertEquals("CHEF", result.get(0).getName());
        assertEquals("WAITER", result.get(1).getName());
        assertEquals("HOSTESS", result.get(2).getName());
    }

    // ID: RoS-6
    // Kiểm tra khi một vai trò không tồn tại
    @Test
    void testGetRolesInRestaurant_RoleNotExist() {
        // Chuẩn bị dữ liệu
        when(roleRepository.findByName("CHEF")).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                roleService.getRolesInRestaurant()
        );
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
        verify(roleRepository, times(1)).findByName("CHEF");
    }

    // ID: RoS-7
    // Kiểm tra khi vai trò tồn tại
    @Test
    void testFindByRoleName_RoleExists() {
        // Chuẩn bị dữ liệu
        String name = "ADMIN";
        Role role = createRole("ADMIN");

        // Cấu hình roleRepository trả về Optional chứa Role
        when(roleRepository.findByName(name)).thenReturn(Optional.of(role));

        // Thực thi
        Role result = roleService.findByRoleName(name);

        // Kiểm tra
        verify(roleRepository, times(1)).findByName(name);
        assertEquals("ADMIN", result.getName());
    }
    // ID: RoS-8
    // Kiểm tra khi vai trò không tồn tại
    @Test
    void testFindByRoleName_RoleNotExists() {
        // Chuẩn bị dữ liệu
        String name = "ADMIN";

        // Cấu hình roleRepository trả về Optional rỗng
        when(roleRepository.findByName(name)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        AppException exception = assertThrows(AppException.class, () ->
                roleService.findByRoleName(name)
        );

        // Xác minh ngoại lệ đúng và repository được gọi
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
        verify(roleRepository, times(1)).findByName(name);
    }

    private RoleRequest createRoleRequest(String name) {
        return RoleRequest.builder()
                .name(name)
                .build();
    }

    private Role createRole(String name) {
        return Role.builder()
                .name(name)
                .build();
    }

    private RoleResponse createRoleResponse(String name) {
        return RoleResponse.builder()
                .name(name)
                .build();
    }
}