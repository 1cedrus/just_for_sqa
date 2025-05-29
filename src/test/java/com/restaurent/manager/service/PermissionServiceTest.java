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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho PermissionService
 * Sử dụng database thật thay vì mock
 * Mục tiêu: Kiểm tra các phương thức service với database integration
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionMapper permissionMapper;

    // Phương thức chạy trước mỗi test case để đảm bảo trạng thái sạch sẽ
    @BeforeEach
    void setup() {
        // Clean up database before each test
        permissionRepository.deleteAll();
    }

    // --- Tests cho createPermission ---
    /**
     * ID: PS-1
     * Test tạo permission thành công với request hợp lệ
     */
    @Test
    void testCreatePermission_Success() {
        // Arrange (Chuẩn bị dữ liệu test)
        PermissionRequest request = new PermissionRequest("VIEW_USER", "View user data");

        // Act (Thực thi phương thức cần kiểm thử)
        PermissionResponse result = permissionService.createPermission(request);

        // Assert (Kiểm tra kết quả)
        assertNotNull(result); // Kết quả không được null
        assertEquals("VIEW_USER", result.getName()); // Name phải khớp
        assertEquals("View user data", result.getDescription()); // Description phải khớp
        assertNotNull(result.getId()); // ID được sinh tự động
        
        // Verify in database - kiểm tra permission đã được lưu đúng trong database
        Permission savedPermission = permissionRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedPermission);
        assertEquals("VIEW_USER", savedPermission.getName());
        assertEquals("View user data", savedPermission.getDescription());
    }

    /**
     * ID: PS-2
     * Test tạo permission với request null
     */
    @Test
    void testCreatePermission_NullRequest() {
        // Act & Assert
        // Thực thi với input null và mong đợi exception
        assertThrows(IllegalArgumentException.class, () -> permissionService.createPermission(null));
        
        // Verify in database - không có permission nào được tạo
        assertEquals(0, permissionRepository.count());
    }

    // --- Tests cho getPermissions ---
    /**
     * ID: PS-3
     * Test lấy danh sách permissions - có dữ liệu
     */
    @Test
    void testGetPermissions_WithData() {
        // Arrange (Chuẩn bị dữ liệu test)
        Permission permission1 = createPermission(null, "VIEW_USER", "View user data");
        Permission permission2 = createPermission(null, "EDIT_USER", "Edit user data");
        permissionRepository.save(permission1);
        permissionRepository.save(permission2);

        // Act (Thực thi phương thức)
        List<PermissionResponse> result = permissionService.getPermissions();

        // Assert (Kiểm tra kết quả)
        assertNotNull(result); // Kết quả không null
        assertEquals(2, result.size()); // Phải có 2 phần tử
        
        // Verify content - kiểm tra nội dung
        assertTrue(result.stream().anyMatch(p -> "VIEW_USER".equals(p.getName())));
        assertTrue(result.stream().anyMatch(p -> "EDIT_USER".equals(p.getName())));
        assertTrue(result.stream().anyMatch(p -> "View user data".equals(p.getDescription())));
        assertTrue(result.stream().anyMatch(p -> "Edit user data".equals(p.getDescription())));
    }

    /**
     * ID: PS-4
     * Test lấy danh sách permissions - không có dữ liệu
     */
    @Test
    void testGetPermissions_EmptyList() {
        // Act (Thực thi phương thức)
        List<PermissionResponse> result = permissionService.getPermissions();

        // Assert (Kiểm tra kết quả)
        assertNotNull(result); // Kết quả không null
        assertTrue(result.isEmpty()); // Danh sách rỗng
        assertEquals(0, result.size()); // Size = 0
    }

    // --- Tests cho updatePermission ---
    /**
     * ID: PS-5
     * Test cập nhật permission thành công
     */
    @Test
    void testUpdatePermission_Success() {
        // Arrange (Chuẩn bị dữ liệu)
        Permission existingPermission = createPermission(null, "VIEW_USER", "View user data");
        existingPermission = permissionRepository.save(existingPermission);
        
        PermissionRequest request = new PermissionRequest("EDIT_USER", "Edit user data");

        // Act (Thực thi phương thức)
        PermissionResponse result = permissionService.updatePermission(existingPermission.getId(), request);

        // Assert (Kiểm tra kết quả)
        assertNotNull(result); // Kết quả không null
        assertEquals("EDIT_USER", result.getName()); // Name đã cập nhật
        assertEquals("Edit user data", result.getDescription()); // Description đã cập nhật
        assertEquals(existingPermission.getId(), result.getId()); // ID không thay đổi
        
        // Verify in database - kiểm tra trong database
        Permission updatedPermission = permissionRepository.findById(existingPermission.getId()).orElse(null);
        assertNotNull(updatedPermission);
        assertEquals("EDIT_USER", updatedPermission.getName());
        assertEquals("Edit user data", updatedPermission.getDescription());
    }

    /**
     * ID: PS-6
     * Test cập nhật permission với ID không tồn tại
     */
    @Test
    void testUpdatePermission_NotFound() {
        // Arrange (Chuẩn bị dữ liệu)
        Long nonExistentId = 999L;
        PermissionRequest request = new PermissionRequest("EDIT_USER", "Edit user data");

        // Act & Assert (Thực thi và mong đợi exception)
        AppException exception = assertThrows(AppException.class,
                () -> permissionService.updatePermission(nonExistentId, request));

        // Verify exception - kiểm tra exception
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode()); // Exception đúng
        
        // Verify database unchanged - database không thay đổi
        assertEquals(0, permissionRepository.count());
    }

    // --- Tests cho findPermissionById ---
    /**
     * ID: PS-7
     * Test tìm permission theo ID - tìm thấy
     */
    @Test
    void testFindPermissionById_Success() {
        // Arrange (Chuẩn bị dữ liệu)
        Permission permission = createPermission(null, "VIEW_USER", "View user data");
        permission = permissionRepository.save(permission);

        // Act (Thực thi phương thức)
        Permission result = permissionService.findPermissionById(permission.getId());

        // Assert (Kiểm tra kết quả)
        assertNotNull(result); // Kết quả không null
        assertEquals(permission.getId(), result.getId()); // ID khớp
        assertEquals("VIEW_USER", result.getName()); // Name khớp
        assertEquals("View user data", result.getDescription()); // Description khớp
    }

    /**
     * ID: PS-8
     * Test tìm permission theo ID - không tìm thấy
     */
    @Test
    void testFindPermissionById_NotFound() {
        // Arrange (Chuẩn bị dữ liệu)
        Long nonExistentId = 999L;

        // Act & Assert (Thực thi và mong đợi exception)
        AppException exception = assertThrows(AppException.class,
                () -> permissionService.findPermissionById(nonExistentId));

        // Verify exception - kiểm tra exception
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode()); // Exception đúng
    }

    // Phương thức hỗ trợ tạo đối tượng Permission
    private Permission createPermission(Long id, String name, String description) {
        Permission perm = new Permission();
        if (id != null) {
            perm.setId(id);
        }
        perm.setPackages(new HashSet<>()); // Khởi tạo để tránh NPE
        perm.setName(name);
        perm.setDescription(description);
        return perm;
    }
}
