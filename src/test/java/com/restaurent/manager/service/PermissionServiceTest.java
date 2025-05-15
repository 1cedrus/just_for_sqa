package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.PermissionRequest;
import com.restaurent.manager.dto.response.PermissionResponse;
import com.restaurent.manager.entity.Permission;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.PermissionMapper;
import com.restaurent.manager.repository.PermissionRepository;
import com.restaurent.manager.service.impl.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test cho PermissionService
 * Sử dụng Mockito để mock PermissionRepository và PermissionMapper
 * Mục tiêu: Đạt branch coverage ~80% cho các phương thức
 */
@ExtendWith(MockitoExtension.class)
public class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository; // Mock repository

    @Mock
    private PermissionMapper permissionMapper; // Mock mapper

    @InjectMocks
    private PermissionService permissionService; // Service cần test

    /**
     * Thiết lập trước mỗi test
     * Khởi tạo các mock objects
     */
    @BeforeEach
    void setUp() {
    }

    // --- Tests cho createPermission ---
    /**
     * ID: PS-1
     * Test tạo permission thành công với request hợp lệ
     */
    @Test
    void testCreatePermission_Success() {
        // Chuẩn bị dữ liệu test
        PermissionRequest request = new PermissionRequest("VIEW_USER", "View user data");
        Permission permission = createPermission(1L, "VIEW_USER", "View user data");
        PermissionResponse response = new PermissionResponse(1L, "VIEW_USER", "View user data");

        // Mock hành vi
        when(permissionMapper.toPermission(request)).thenReturn(permission); // Map request -> entity
        when(permissionRepository.save(permission)).thenReturn(permission); // Lưu entity
        when(permissionMapper.toPermissionResponse(permission)).thenReturn(response); // Map entity -> response

        // Thực thi phương thức
        PermissionResponse result = permissionService.createPermission(request);

        // Kiểm tra kết quả
        assertNotNull(result); // Kết quả không được null
        assertEquals(response.getId(), result.getId()); // ID phải khớp
        assertEquals(response.getName(), result.getName()); // Name phải khớp
        verify(permissionMapper, times(1)).toPermission(request); // Gọi mapper đúng 1 lần
        verify(permissionRepository, times(1)).save(permission); // Gọi save đúng 1 lần
        verify(permissionMapper, times(1)).toPermissionResponse(permission); // Gọi response mapper đúng 1 lần
    }

    /**
     * ID: PS-2
     * Test tạo permission với request null
     */
    @Test
    void testCreatePermission_NullRequest() {
        // Thực thi với input null và mong đợi exception
        assertThrows(IllegalArgumentException.class, () -> permissionService.createPermission(null));

        // Xác minh không gọi repository hoặc mapper
        verifyNoInteractions(permissionMapper, permissionRepository);
    }

    // --- Tests cho getPermissions ---
    /**
     * ID: PS-3
     * Test lấy danh sách permissions - có dữ liệu
     */
    @Test
    void testGetPermissions_WithData() {
        // Chuẩn bị dữ liệu test
        Permission permission1 = createPermission(1L, "VIEW_USER", "View user data");
        Permission permission2 = createPermission(2L, "EDIT_USER", "Edit user data");
        List<Permission> permissions = List.of(permission1, permission2);
        PermissionResponse response1 = new PermissionResponse(1L, "VIEW_USER", "View user data");
        PermissionResponse response2 = new PermissionResponse(2L, "EDIT_USER", "Edit user data");

        // Mock hành vi
        when(permissionRepository.findAll()).thenReturn(permissions); // Trả về danh sách permissions
        when(permissionMapper.toPermissionResponse(permission1)).thenReturn(response1); // Map permission1
        when(permissionMapper.toPermissionResponse(permission2)).thenReturn(response2); // Map permission2

        // Thực thi
        List<PermissionResponse> result = permissionService.getPermissions();

        // Kiểm tra
        assertNotNull(result); // Kết quả không null
        assertEquals(2, result.size()); // Phải có 2 phần tử
        assertEquals("VIEW_USER", result.get(0).getName()); // Phần tử 1 đúng
        assertEquals("EDIT_USER", result.get(1).getName()); // Phần tử 2 đúng
        verify(permissionRepository, times(1)).findAll(); // Gọi findAll 1 lần
        verify(permissionMapper, times(2)).toPermissionResponse(any(Permission.class)); // Gọi mapper 2 lần
    }

    /**
     * ID: PS-4
     * Test lấy danh sách permissions - không có dữ liệu
     */
    @Test
    void testGetPermissions_EmptyList() {
        // Mock hành vi trả về danh sách rỗng
        when(permissionRepository.findAll()).thenReturn(List.of());

        // Thực thi
        List<PermissionResponse> result = permissionService.getPermissions();

        // Kiểm tra
        assertNotNull(result); // Kết quả không null
        assertTrue(result.isEmpty()); // Danh sách rỗng
        verify(permissionRepository, times(1)).findAll(); // Gọi findAll 1 lần
        verifyNoInteractions(permissionMapper); // Không gọi mapper vì danh sách rỗng
    }

    // --- Tests cho updatePermission ---
    /**
     * ID: PS-5
     * Test cập nhật permission thành công
     */
    @Test
    void testUpdatePermission_Success() {
        // Chuẩn bị dữ liệu
        Long permissionId = 1L;
        PermissionRequest request = new PermissionRequest("EDIT_USER", "Edit user data");
        Permission existingPermission = createPermission(1L, "VIEW_USER", "View user data");
        Permission updatedPermission = createPermission(1L, "EDIT_USER", "Edit user data");
        PermissionResponse response = new PermissionResponse(1L, "EDIT_USER", "Edit user data");

        // Mock hành vi
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(existingPermission)); // Tìm thấy permission
        doNothing().when(permissionMapper).updatePermission(existingPermission, request); // Cập nhật entity
        when(permissionRepository.save(existingPermission)).thenReturn(updatedPermission); // Lưu entity
        when(permissionMapper.toPermissionResponse(updatedPermission)).thenReturn(response); // Map response

        // Thực thi
        PermissionResponse result = permissionService.updatePermission(permissionId, request);

        // Kiểm tra
        assertNotNull(result); // Kết quả không null
        assertEquals("EDIT_USER", result.getName()); // Name đã cập nhật
        verify(permissionRepository, times(1)).findById(permissionId); // Gọi findById
        verify(permissionMapper, times(1)).updatePermission(existingPermission, request); // Gọi update
        verify(permissionRepository, times(1)).save(existingPermission); // Gọi save
        verify(permissionMapper, times(1)).toPermissionResponse(updatedPermission); // Gọi response mapper
    }

    /**
     * ID: PS-6
     * Test cập nhật permission với ID không tồn tại
     */
    @Test
    void testUpdatePermission_NotFound() {
        // Chuẩn bị dữ liệu
        Long permissionId = 1L;
        PermissionRequest request = new PermissionRequest("EDIT_USER", "Edit user data");

        // Mock hành vi: không tìm thấy permission
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        // Thực thi và mong đợi exception
        AppException exception = assertThrows(AppException.class,
                () -> permissionService.updatePermission(permissionId, request));

        // Kiểm tra
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode()); // Exception đúng
        verify(permissionRepository, times(1)).findById(permissionId); // Gọi findById
        verifyNoMoreInteractions(permissionMapper, permissionRepository); // Không gọi thêm
    }

    // --- Tests cho findPermissionById ---
    /**
     * ID: PS-7
     * Test tìm permission theo ID - tìm thấy
     */
    @Test
    void testFindPermissionById_Success() {
        // Chuẩn bị dữ liệu
        Long permissionId = 1L;
        Permission permission = createPermission(1L, "VIEW_USER", "View user data");

        // Mock hành vi
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        // Thực thi
        Permission result = permissionService.findPermissionById(permissionId);

        // Kiểm tra
        assertNotNull(result); // Kết quả không null
        assertEquals(permissionId, result.getId()); // ID khớp
        assertEquals("VIEW_USER", result.getName()); // Name khớp
        verify(permissionRepository, times(1)).findById(permissionId); // Gọi findById
    }

    /**
     * ID: PS-8
     * Test tìm permission theo ID - không tìm thấy
     */
    @Test
    void testFindPermissionById_NotFound() {
        // Chuẩn bị dữ liệu
        Long permissionId = 1L;

        // Mock hành vi: không tìm thấy
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        // Thực thi và mong đợi exception
        AppException exception = assertThrows(AppException.class,
                () -> permissionService.findPermissionById(permissionId));

        // Kiểm tra
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode()); // Exception đúng
        verify(permissionRepository, times(1)).findById(permissionId); // Gọi findById
    }

    private Permission createPermission(Long id, String name, String description) {
        Permission perm = new Permission();
        perm.setId(id);
        perm.setPackages(new HashSet<>()); // Khởi tạo để tránh NPE
        perm.setName(name);
        perm.setDescription(description);
        return perm;
    }
}
