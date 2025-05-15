package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.PackageRequest;
import com.restaurent.manager.dto.response.Pack.PackUpgradeResponse;
import com.restaurent.manager.dto.response.Pack.PackageResponse;
import com.restaurent.manager.entity.Permission;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.PackageMapper;
import com.restaurent.manager.repository.PackageRepository;
import com.restaurent.manager.repository.PermissionRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.service.impl.PackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Sử dụng MockitoExtension để tích hợp Mockito với JUnit 5, cho phép sử dụng các annotation như @Mock, @InjectMocks
@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

    // Mô phỏng PackageMapper để kiểm soát việc ánh xạ giữa request/response và entity
    @Mock
    private PackageMapper packageMapper;

    // Mô phỏng PermissionRepository để giả lập việc truy xuất danh sách quyền từ cơ sở dữ liệu
    @Mock
    private PermissionRepository permissionRepository;

    // Mô phỏng PackageRepository để giả lập việc lưu gói vào cơ sở dữ liệu
    @Mock
    private PackageRepository packageRepository;

    // Mô phỏng RestaurantRepository để giả lập lớp làm việc với Restaurant vào cơ sở dữ liệu
    @Mock
    private RestaurantRepository restaurantRepository;

    // InjectMocks tự động tạo instance của PackageService và tiêm các mock vào constructor của nó
    @InjectMocks
    private PackageService packageService;

    // Phương thức chạy trước mỗi test case để đảm bảo trạng thái sạch sẽ
    @BeforeEach
    void setup() {
        // Không cần khởi tạo thủ công vì @InjectMocks đã xử lý việc này
    }

    // Test case 1: Trường hợp bình thường với yêu cầu hợp lệ và quyền tồn tại
    // ID: PkS-1
    @Test
    void testCreate_NormalCase() {
        // Arrange (chuẩn bị dữ liệu và cấu hình mock)
        // Tạo một PackageRequest với tên gói và danh sách ID quyền hợp lệ
        String packName = "testPackage";
        Set<Long> permissionIds = Set.of(1L, 2L);
        PackageRequest request = createRequest(packName, permissionIds);

        // Tạo một Package giả để trả về từ packageMapper.toPackage
        Package pack = createPackage(101L, packName);

        // Tạo danh sách quyền giả để trả về từ permissionRepository.findAllById
        List<Permission> permissions = Arrays.asList(createPermission(1L), createPermission(2L));

        // Cấu hình hành vi của packageMapper: khi gọi toPackage(request), trả về pack
        when(packageMapper.toPackage(request)).thenReturn(pack);

        // Cấu hình permissionRepository: khi tìm quyền theo danh sách ID, trả về danh sách permissions
        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);

        // Cấu hình packageRepository: khi lưu gói, trả về chính đối tượng được truyền vào
        // Sử dụng any() để khớp với bất kỳ Package nào, sau đó trả về đối tượng đầu vào
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Cấu hình packageMapper: khi ánh xạ từ Package sang PackageResponse, trả về một response dựa trên Package
        when(packageMapper.toPackResponse(any(Package.class))).thenAnswer(invocation -> {
            Package p = invocation.getArgument(0);
            return createPackageResponse(p.getId(), p.getPackName());
        });

        // Act (thực thi phương thức cần kiểm thử)
        PackageResponse response = packageService.create(request);

        // Assert (kiểm tra kết quả)
        // Xác minh packageMapper đã được gọi để ánh xạ request thành entity
        verify(packageMapper).toPackage(request);

        // Xác minh permissionRepository đã được gọi với đúng danh sách ID quyền
        verify(permissionRepository).findAllById(permissionIds);

        // Sử dụng ArgumentCaptor để bắt đối tượng Package được truyền vào packageRepository.save
        ArgumentCaptor<Package> packageCaptor = ArgumentCaptor.forClass(Package.class);
        verify(packageRepository).save(packageCaptor.capture());
        Package capturedPack = packageCaptor.getValue();

        // Kiểm tra tên gói đã được chuyển thành chữ in hoa (uppercase)
        assertEquals("TESTPACKAGE", capturedPack.getPackName());

        // Kiểm tra quyền đã được liên kết đúng với gói (setPermissions)
        assertEquals(new HashSet<>(permissions), capturedPack.getPermissions());

        // Kiểm tra mối quan hệ ngược: gói đã được thêm vào danh sách packages của mỗi permission
        for (Permission perm : permissions) {
            assertTrue(perm.getPackages().contains(capturedPack));
        }

        // Kiểm tra phản hồi không null và chứa thông tin đúng
        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("TESTPACKAGE", response.getPackName());
    }

    // Test case 2: Trường hợp danh sách quyền rỗng
    // ID: PkS-2
    @Test
    void testCreate_NoPermissions() {
        // Arrange
        // Tạo request với danh sách quyền rỗng
        PackageRequest request = createRequest("testPackage", Collections.emptySet());
        Package pack = createPackage(101L, "testPackage");

        when(packageMapper.toPackage(request)).thenReturn(pack);
        when(permissionRepository.findAllById(request.getPermissions())).thenReturn(Collections.emptyList());
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(packageMapper.toPackResponse(any(Package.class))).thenAnswer(invocation -> {
            Package p = invocation.getArgument(0);
            return createPackageResponse(p.getId(), p.getPackName());
        });

        // Act
        PackageResponse response = packageService.create(request);

        // Assert
        // Bắt đối tượng Package được lưu để kiểm tra
        ArgumentCaptor<Package> packageCaptor = ArgumentCaptor.forClass(Package.class);
        verify(packageRepository).save(packageCaptor.capture());
        Package capturedPack = packageCaptor.getValue();

        // Kiểm tra tên gói vẫn được chuyển thành chữ in hoa
        assertEquals("TESTPACKAGE", capturedPack.getPackName());

        // Kiểm tra không có quyền nào được liên kết
        assertTrue(capturedPack.getPermissions().isEmpty());

        // Kiểm tra phản hồi hợp lệ
        assertNotNull(response);
        assertEquals("TESTPACKAGE", response.getPackName());
    }

    // Test case 3: Trường hợp một số quyền không tồn tại
    // ID: PkS-3
    @Test
    void testCreate_InvalidPermissionIds() {
        // Arrange
        // Tạo request với danh sách quyền, trong đó chỉ một số tồn tại
        Set<Long> permissionIds = Set.of(1L, 3L); // 3L không tồn tại
        PackageRequest request = createRequest("testPackage", permissionIds);
        Package pack = createPackage(101L, "testPackage");
        List<Permission> permissions = Collections.singletonList(createPermission(1L)); // Chỉ 1L tồn tại

        when(packageMapper.toPackage(request)).thenReturn(pack);
        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(packageMapper.toPackResponse(any(Package.class))).thenAnswer(invocation -> {
            Package p = invocation.getArgument(0);
            return createPackageResponse(p.getId(), p.getPackName());
        });

        // Act
        PackageResponse response = packageService.create(request);

        // Assert
        ArgumentCaptor<Package> packageCaptor = ArgumentCaptor.forClass(Package.class);
        verify(packageRepository).save(packageCaptor.capture());
        Package capturedPack = packageCaptor.getValue();

        // Kiểm tra chỉ quyền tồn tại (1L) được liên kết
        assertEquals(1, capturedPack.getPermissions().size());
        assertTrue(capturedPack.getPermissions().contains(permissions.get(0)));

        // Kiểm tra phản hồi
        assertEquals("TESTPACKAGE", response.getPackName());
    }

    // Test case 4: Trường hợp danh sách quyền null
    // ID: PkS-4
    @Test
    void testCreate_NullPermissions() {
        // Arrange
        // Tạo request với permissions null
        PackageRequest request = createRequest("testPackage", null);
        Package pack = createPackage(101L, "testPackage");

        when(packageMapper.toPackage(request)).thenReturn(pack);
        when(permissionRepository.findAllById(null)).thenReturn(Collections.emptyList()); // Giả định xử lý null
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(packageMapper.toPackResponse(any(Package.class))).thenAnswer(invocation -> {
            Package p = invocation.getArgument(0);
            return createPackageResponse(p.getId(), p.getPackName());
        });

        // Act
        PackageResponse response = packageService.create(request);

        // Assert
        ArgumentCaptor<Package> packageCaptor = ArgumentCaptor.forClass(Package.class);
        verify(packageRepository).save(packageCaptor.capture());
        Package capturedPack = packageCaptor.getValue();

        // Kiểm tra không có quyền nào được liên kết khi permissions là null
        assertTrue(capturedPack.getPermissions().isEmpty());
        assertEquals("TESTPACKAGE", response.getPackName());
    }

    // Test case 5: Trường hợp tên gói null (kiểm tra ngoại lệ)
    // ID: PkS-5
    @Test
    void testCreate_NullPackName() {
        // Arrange
        PackageRequest request = createRequest(null, Collections.emptySet());
        Package pack = createPackage(101L, null); // packName null

        when(packageMapper.toPackage(request)).thenReturn(pack);

        // Act & Assert
        // Kiểm tra xem phương thức ném NullPointerException khi gọi toUpperCase trên null
        assertThrows(NullPointerException.class, () -> packageService.create(request));
    }

    // Test case 1: Kiểm tra khi danh sách Package không rỗng
    // ID: PkS-6
    @Test
    void testGetPacks_NonEmptyList() {
        // Arrange (Chuẩn bị dữ liệu và cấu hình mock)
        // Tạo danh sách Package giả với 2 phần tử
        Package pack1 = createPackage(1L, "BASIC");
        Package pack2 = createPackage(2L, "PREMIUM");
        List<Package> packages = Arrays.asList(pack1, pack2);

        // Tạo danh sách PackageResponse giả tương ứng
        PackageResponse response1 = createPackageResponse(1L, "BASIC");
        PackageResponse response2 = createPackageResponse(2L, "PREMIUM");

        // Cấu hình packageRepository trả về danh sách Package
        when(packageRepository.findAll()).thenReturn(packages);

        // Cấu hình packageMapper ánh xạ Package sang PackageResponse
        when(packageMapper.toPackResponse(pack1)).thenReturn(response1);
        when(packageMapper.toPackResponse(pack2)).thenReturn(response2);

        // Cấu hình restaurantRepository trả về số lượng Restaurant cho từng Package ID
        when(restaurantRepository.countByRestaurantPackage_Id(1L)).thenReturn(5);
        when(restaurantRepository.countByRestaurantPackage_Id(2L)).thenReturn(3);

        // Act (Thực thi phương thức cần kiểm thử)
        List<PackageResponse> result = packageService.getPacks();

        // Assert (Kiểm tra kết quả)
        // Xác minh packageRepository.findAll được gọi đúng 1 lần
        verify(packageRepository, times(1)).findAll();

        // Xác minh packageMapper được gọi cho từng Package
        verify(packageMapper, times(1)).toPackResponse(pack1);
        verify(packageMapper, times(1)).toPackResponse(pack2);

        // Xác minh restaurantRepository được gọi để đếm số Restaurant cho từng Package ID
        verify(restaurantRepository, times(1)).countByRestaurantPackage_Id(1L);
        verify(restaurantRepository, times(1)).countByRestaurantPackage_Id(2L);

        // Kiểm tra kích thước danh sách trả về
        assertEquals(2, result.size());

        // Kiểm tra chi tiết từng PackageResponse trong kết quả
        PackageResponse result1 = result.getFirst();
        assertEquals(1L, result1.getId());
        assertEquals("BASIC", result1.getPackName());
        assertEquals(5, result1.getTotal()); // Tổng số Restaurant được set đúng

        PackageResponse result2 = result.get(1);
        assertEquals(2L, result2.getId());
        assertEquals("PREMIUM", result2.getPackName());
        assertEquals(3, result2.getTotal()); // Tổng số Restaurant được set đúng
    }

    // Test case 2: Kiểm tra khi danh sách Package rỗng
    // ID: PkS-7
    @Test
    void testGetPacks_EmptyList() {
        // Arrange
        // Cấu hình packageRepository trả về danh sách rỗng
        when(packageRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PackageResponse> result = packageService.getPacks();

        // Assert
        // Xác minh packageRepository.findAll được gọi 1 lần
        verify(packageRepository, times(1)).findAll();

        // Xác minh không có tương tác nào với packageMapper vì không có Package để ánh xạ
        verify(packageMapper, never()).toPackResponse(any());

        // Xác minh không có tương tác nào với restaurantRepository vì không có PackageResponse để đếm
        verify(restaurantRepository, never()).countByRestaurantPackage_Id(anyLong());

        // Kiểm tra danh sách trả về rỗng
        assertTrue(result.isEmpty());
    }

    // Test case 1: Kiểm tra trường hợp thành công khi cả Permission và Package tồn tại
    // ID: PkS-8
    @Test
    void testAddPermission_Success() {
        // Arrange (Chuẩn bị dữ liệu và cấu hình mock)
        Long permissionId = 1L;
        Long packId = 2L;

        // Tạo Permission và Package giả
        Permission permission = createPermission(permissionId);
        Package pack = createPackage(packId, "TEST_PACK");
        PackageResponse response = createPackageResponse(packId, "TEST_PACK");

        // Cấu hình permissionRepository trả về Permission trong Optional
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        // Cấu hình packageRepository trả về Package trong Optional
        when(packageRepository.findById(packId)).thenReturn(Optional.of(pack));

        // Cấu hình packageRepository.save trả về Package đã lưu (giả lập không thay đổi)
        when(packageRepository.save(pack)).thenReturn(pack);

        // Cấu hình packageMapper ánh xạ Package sang PackageResponse
        when(packageMapper.toPackResponse(pack)).thenReturn(response);

        // Act (Thực thi phương thức cần kiểm thử)
        PackageResponse result = packageService.addPermission(permissionId, packId);

        // Assert (Kiểm tra kết quả)
        // Xác minh các repository được gọi đúng
        verify(permissionRepository, times(1)).findById(permissionId);
        verify(packageRepository, times(1)).findById(packId);
        verify(packageRepository, times(1)).save(pack);

        // Xác minh packageMapper được gọi để ánh xạ
        verify(packageMapper, times(1)).toPackResponse(pack);

        assertEquals(1, pack.getPermissions().size());
        assertEquals(1, permission.getPackages().size());

        // Kiểm tra kết quả trả về
        assertNotNull(result);
        assertEquals(packId, result.getId());
        assertEquals("TEST_PACK", result.getPackName());
    }

    // Test case 2: Kiểm tra khi Permission không tồn tại
    // ID: PkS-9
    @Test
    void testAddPermission_PermissionNotFound() {
        // Arrange
        Long permissionId = 1L;
        Long packId = 2L;

        // Cấu hình permissionRepository trả về Optional rỗng (Permission không tồn tại)
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        // Act & Assert (Thực thi và kiểm tra ngoại lệ)
        AppException exception = assertThrows(AppException.class, () ->
                packageService.addPermission(permissionId, packId)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.INVALID_KEY, exception.getErrorCode());

        // Xác minh chỉ permissionRepository được gọi, packageRepository không được gọi
        verify(permissionRepository, times(1)).findById(permissionId);
        verify(packageRepository, never()).findById(anyLong());
    }

    // Test case 3: Kiểm tra khi Package không tồn tại
    // ID: PkS-10
    @Test
    void testAddPermission_PackageNotFound() {
        // Arrange
        Long permissionId = 1L;
        Long packId = 2L;
        Permission permission = createPermission(permissionId);

        // Cấu hình permissionRepository trả về Permission
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));

        // Cấu hình packageRepository trả về Optional rỗng (Package không tồn tại)
        when(packageRepository.findById(packId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.addPermission(permissionId, packId)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.INVALID_KEY, exception.getErrorCode());

        // Xác minh cả hai repository được gọi theo thứ tự
        verify(permissionRepository, times(1)).findById(permissionId);
        verify(packageRepository, times(1)).findById(packId);
    }

    // Test case 1: Thành công với danh sách quyền không rỗng
    // ID: PkS-11
    @Test
    void testUpdatePackage_SuccessWithPermissions() {
        // Arrange (Chuẩn bị dữ liệu và cấu hình mock)
        Long packId = 1L;
        PackageRequest request = createRequest("UpdatedPack", Set.of(1L, 2L));
        Package pack = createPackage(packId, "OriginalPack");
        List<Permission> permissions = Arrays.asList(createPermission(1L), createPermission(2L));
        PackageResponse response = createPackageResponse(packId, "UpdatedPack");

        // Sửa: Mock packageRepository.findById thay vì packageService.findPackById
        when(packageRepository.findById(packId)).thenReturn(Optional.of(pack));

        // Giả lập permissionRepository trả về danh sách Permission
        when(permissionRepository.findAllById(request.getPermissions())).thenReturn(permissions);

        // Giả lập packageRepository.save trả về Package đã lưu
        when(packageRepository.save(pack)).thenReturn(pack);

        // Giả lập packageMapper.toPackResponse trả về PackageResponse
        when(packageMapper.toPackResponse(pack)).thenReturn(response);

        // Act (Thực thi phương thức cần kiểm thử)
        PackageResponse result = packageService.updatePackage(packId, request);

        // Assert (Kiểm tra kết quả)
        // Xác minh packageRepository.findById được gọi với packId
        verify(packageRepository, times(1)).findById(packId);

        // Xác minh packageMapper.updatePackage được gọi để cập nhật Package
        verify(packageMapper, times(1)).updatePackage(pack, request);

        // Xác minh permissionRepository được gọi với danh sách ID quyền
        verify(permissionRepository, times(1)).findAllById(request.getPermissions());

        // Xác minh pack.setPermissions được gọi với danh sách quyền
        assertEquals(new HashSet<>(permissions), pack.getPermissions());

        // Xác minh mỗi Permission đã thêm Package vào danh sách packages của nó
        permissions.forEach(permission ->
                assertTrue(permission.getPackages().contains(pack))
        );

        // Xác minh packageRepository.save được gọi
        verify(packageRepository, times(1)).save(pack);

        // Xác minh packageMapper.toPackResponse được gọi
        verify(packageMapper, times(1)).toPackResponse(pack);

        // Kiểm tra kết quả trả về
        assertNotNull(result);
        assertEquals(packId, result.getId());
        assertEquals("UpdatedPack", result.getPackName());
    }

    // Test case 2: Thành công với danh sách quyền rỗng
    // ID: PkS-12
    @Test
    void testUpdatePackage_EmptyPermissions() {
        // Arrange
        Long packId = 1L;
        PackageRequest request = createRequest("UpdatedPack", Collections.emptySet());
        Package pack = createPackage(packId, "OriginalPack");
        PackageResponse response = createPackageResponse(packId, "UpdatedPack");

        when(packageRepository.findById(packId)).thenReturn(Optional.of(pack));
        when(permissionRepository.findAllById(request.getPermissions())).thenReturn(Collections.emptyList());
        when(packageRepository.save(pack)).thenReturn(pack);
        when(packageMapper.toPackResponse(pack)).thenReturn(response);

        // Act
        PackageResponse result = packageService.updatePackage(packId, request);

        // Assert
        verify(packageRepository, times(1)).findById(packId);
        verify(packageMapper, times(1)).updatePackage(pack, request);
        verify(permissionRepository, times(1)).findAllById(request.getPermissions());
        verify(packageRepository, times(1)).save(pack);
        verify(packageMapper, times(1)).toPackResponse(pack);

        // Kiểm tra permissions không bị thay đổi (vẫn là tập rỗng từ createPackage)
        assertTrue(pack.getPermissions().isEmpty());

        // Kiểm tra kết quả trả về
        assertNotNull(result);
        assertEquals(packId, result.getId());
        assertEquals("UpdatedPack", result.getPackName());
    }

    // Test case 3: Package không tồn tại
    // ID: PkS-13
    @Test
    void testUpdatePackage_PackageNotFound() {
        // Arrange
        Long packId = 1L;
        PackageRequest request = createRequest("UpdatedPack", Set.of(1L, 2L));

        // Giả lập findPackById ném ngoại lệ
        when(packageRepository.findById(packId)).thenThrow(new AppException(ErrorCode.INVALID_KEY));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.updatePackage(packId, request)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.INVALID_KEY, exception.getErrorCode());

        // Xác minh chỉ findPackById được gọi, các bước sau không thực thi
        verify(packageRepository, times(1)).findById(packId);
        verify(packageMapper, never()).updatePackage(any(), any());
        verify(permissionRepository, never()).findAllById(any());
    }

    // Test case 1: Thành công khi Package tồn tại
    // ID: PkS-14
    @Test
    void testFindPackById_Success() {
        // Arrange (Chuẩn bị dữ liệu và cấu hình mock)
        Long packId = 1L;
        Package pack = createPackage(packId, "TestPack");

        // Cấu hình packageRepository trả về Optional chứa Package
        when(packageRepository.findById(packId)).thenReturn(Optional.of(pack));

        // Act (Thực thi phương thức cần kiểm thử)
        Package result = packageService.findPackById(packId);

        // Assert (Kiểm tra kết quả)
        // Xác minh packageRepository.findById được gọi đúng 1 lần với packId
        verify(packageRepository, times(1)).findById(packId);

        // Kiểm tra kết quả trả về không null và đúng Package mong đợi
        assertNotNull(result);
        assertEquals(packId, result.getId());
        assertEquals("TestPack", result.getPackName());
    }

    // Test case 2: Thất bại khi Package không tồn tại
    // ID: PkS-15
    @Test
    void testFindPackById_PackageNotFound() {
        // Arrange
        Long packId = 1L;

        // Cấu hình packageRepository trả về Optional rỗng (Package không tồn tại)
        when(packageRepository.findById(packId)).thenReturn(Optional.empty());

        // Act & Assert (Thực thi và kiểm tra ngoại lệ)
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findPackById(packId)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());

        // Xác minh packageRepository.findById được gọi đúng 1 lần với packId
        verify(packageRepository, times(1)).findById(packId);
    }

    // Test case 1: Thành công khi Package tồn tại
    // ID: PkS-16
    @Test
    void testFindByPackName_Success() {
        // Arrange (Chuẩn bị dữ liệu và cấu hình mock)
        String packName = "TestPack";
        Package pack = createPackage(1L, packName);

        // Cấu hình packageRepository trả về Optional chứa Package khi tìm theo tên
        when(packageRepository.findByPackName(packName)).thenReturn(Optional.of(pack));

        // Act (Thực thi phương thức cần kiểm thử)
        Package result = packageService.findByPackName(packName);

        // Assert (Kiểm tra kết quả)
        // Xác minh packageRepository.findByPackName được gọi đúng 1 lần với packName
        verify(packageRepository, times(1)).findByPackName(packName);

        // Kiểm tra kết quả trả về không null và đúng Package mong đợi
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(packName, result.getPackName());
    }

    // Test case 2: Thất bại khi Package không tồn tại
    // ID: PkS-17
    @Test
    void testFindByPackName_PackageNotFound() {
        // Arrange
        String packName = "NonExistentPack";

        // Cấu hình packageRepository trả về Optional rỗng (Package không tồn tại)
        when(packageRepository.findByPackName(packName)).thenReturn(Optional.empty());

        // Act & Assert (Thực thi và kiểm tra ngoại lệ)
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findByPackName(packName)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.PACKAGE_NOT_EXIST, exception.getErrorCode());

        // Xác minh packageRepository.findByPackName được gọi đúng 1 lần với packName
        verify(packageRepository, times(1)).findByPackName(packName);
    }

    // Test case 3: PackageName là null, giả định repository trả về Optional.empty()
    // ID: PkS-18
    @Test
    void testFindByPackName_NullName_ReturnsEmpty() {
        // Arrange
        String packName = null;

        // Cấu hình packageRepository trả về Optional rỗng khi name là null
        when(packageRepository.findByPackName(null)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findByPackName(packName)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.PACKAGE_NOT_EXIST, exception.getErrorCode());

        // Xác minh packageRepository.findByPackName được gọi với null
        verify(packageRepository, times(1)).findByPackName(null);
    }

    // Test case 1: Thành công với daysLeft > 0
    // ID: PkS-19
    @Test
    void testFindPacksToUpgradeForRestaurant_Success_DaysLeftPositive() {
        // Arrange
        Long restaurantId = 1L;
        Long currentPackId = 2L;
        Restaurant restaurant = createRestaurant(restaurantId, currentPackId, LocalDateTime.now().plusDays(10));
        Package currentPack = createPackage(currentPackId, "Basic", 100.0);
        Package upgradePack = createPackage(3L, "Premium", 200.0);
        PackageResponse upgradeResponse = createPackageResponse(3L, "Premium");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageRepository.findById(currentPackId)).thenReturn(Optional.of(currentPack));
        when(packageRepository.findByPricePerMonthGreaterThan(100.0)).thenReturn(Collections.singletonList(upgradePack));
        when(packageMapper.toPackResponse(upgradePack)).thenReturn(upgradeResponse);

        // Act
        PackUpgradeResponse result = packageService.findPacksToUpgradeForRestaurant(restaurantId);

        // Assert
        verify(restaurantRepository, times(1)).findById(restaurantId);
        verify(packageRepository, times(1)).findById(currentPackId);
        verify(packageRepository, times(1)).findByPricePerMonthGreaterThan(100.0);
        verify(packageMapper, times(1)).toPackResponse(upgradePack);

        assertNotNull(result);
        assertEquals(1, result.getPackages().size());
        assertEquals("Premium", result.getPackages().getFirst().getPackName());

        // Tính deposit: 100 / 31 * 10 = ~32.26, làm tròn lên thành 33
        assertEquals(33.0, result.getDeposit(), 0.01);
    }

    // Test case 2: Thành công với daysLeft <= 0
    // ID: PkS-20
    @Test
    void testFindPacksToUpgradeForRestaurant_Success_DaysLeftZeroOrNegative() {
        // Arrange
        Long restaurantId = 1L;
        Long currentPackId = 2L;
        Restaurant restaurant = createRestaurant(restaurantId, currentPackId, LocalDateTime.now().minusDays(1));
        Package currentPack = createPackage(currentPackId, "Basic", 100.0);
        Package upgradePack = createPackage(3L, "Premium", 200.0);
        PackageResponse upgradeResponse = createPackageResponse(3L, "Premium");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(packageRepository.findById(currentPackId)).thenReturn(Optional.of(currentPack));
        when(packageRepository.findByPricePerMonthGreaterThan(100.0)).thenReturn(Collections.singletonList(upgradePack));
        when(packageMapper.toPackResponse(upgradePack)).thenReturn(upgradeResponse);

        // Act
        PackUpgradeResponse result = packageService.findPacksToUpgradeForRestaurant(restaurantId);

        // Assert
        verify(restaurantRepository, times(1)).findById(restaurantId);
        verify(packageRepository, times(1)).findById(currentPackId);
        verify(packageRepository, times(1)).findByPricePerMonthGreaterThan(100.0);
        verify(packageMapper, times(1)).toPackResponse(upgradePack);

        assertNotNull(result);
        assertEquals(1, result.getPackages().size());
        assertEquals("Premium", result.getPackages().getFirst().getPackName());

        // daysLeft <= 0 nên deposit = 0
        assertEquals(0.0, result.getDeposit(), 0.01);
    }

    // Test case 3: Restaurant không tồn tại
    // ID: PkS-21
    @Test
    void testFindPacksToUpgradeForRestaurant_RestaurantNotFound() {
        // Arrange
        Long restaurantId = 1L;

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findPacksToUpgradeForRestaurant(restaurantId)
        );

        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
        verify(restaurantRepository, times(1)).findById(restaurantId);
        verify(packageRepository, never()).findById(anyLong());
    }

    // Test case 1: Danh sách Package không rỗng
    // ID: PkS-22
    @Test
    void testGetPacksView_NonEmptyList() {
        // Arrange (Chuẩn bị dữ liệu và cấu hình mock)
        Package pack1 = createPackage(1L, "Basic");
        Package pack2 = createPackage(2L, "Premium");
        List<Package> packages = Arrays.asList(pack1, pack2);

        PackageResponse response1 = createPackageResponse(1L, "Basic");
        PackageResponse response2 = createPackageResponse(2L, "Premium");

        // Cấu hình packageRepository trả về danh sách Package không rỗng
        when(packageRepository.findAll()).thenReturn(packages);

        // Cấu hình packageMapper ánh xạ từng Package sang PackageResponse
        when(packageMapper.toPackResponse(pack1)).thenReturn(response1);
        when(packageMapper.toPackResponse(pack2)).thenReturn(response2);

        // Act (Thực thi phương thức cần kiểm thử)
        List<PackageResponse> result = packageService.getPacksView();

        // Assert (Kiểm tra kết quả)
        // Xác minh packageRepository.findAll được gọi đúng 1 lần
        verify(packageRepository, times(1)).findAll();

        // Xác minh packageMapper được gọi cho từng Package
        verify(packageMapper, times(1)).toPackResponse(pack1);
        verify(packageMapper, times(1)).toPackResponse(pack2);

        // Kiểm tra kích thước và nội dung danh sách trả về
        assertEquals(2, result.size());
        assertEquals("Basic", result.get(0).getPackName());
        assertEquals("Premium", result.get(1).getPackName());
    }

    // Test case 2: Danh sách Package rỗng
    // ID: PkS-23
    @Test
    void testGetPacksView_EmptyList() {
        // Arrange
        // Cấu hình packageRepository trả về danh sách rỗng
        when(packageRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PackageResponse> result = packageService.getPacksView();

        // Assert
        // Xác minh packageRepository.findAll được gọi đúng 1 lần
        verify(packageRepository, times(1)).findAll();

        // Xác minh packageMapper không được gọi vì không có Package để ánh xạ
        verify(packageMapper, never()).toPackResponse(any());

        // Kiểm tra danh sách trả về rỗng
        assertTrue(result.isEmpty());
    }

    // Các phương thức hỗ trợ tạo đối tượng giả
    private Restaurant createRestaurant(Long id, Long packId, LocalDateTime expiryDate) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        Package pack = createPackage(packId, "Basic", 100.0);
        restaurant.setRestaurantPackage(pack);
        restaurant.setExpiryDate(expiryDate);
        return restaurant;
    }

    // Các phương thức hỗ trợ tạo đối tượng giả
    private PackageRequest createRequest(String packName, Set<Long> permissionIds) {
        PackageRequest request = new PackageRequest();
        request.setPackName(packName);
        request.setPermissions(permissionIds);
        return request;
    }

    private Package createPackage(Long id, String packName) {
        Package pack = new Package();
        pack.setId(id);
        pack.setPackName(packName);
        pack.setPermissions(new HashSet<>()); // Khởi tạo để tránh NPE
        return pack;
    }

    private Package createPackage(Long id, String packName, double pricePerMonth) {
        Package pack = new Package();
        pack.setId(id);
        pack.setPackName(packName);
        pack.setPermissions(new HashSet<>()); // Khởi tạo để tránh NPE
        pack.setPricePerMonth(pricePerMonth);
        return pack;
    }

    private Permission createPermission(Long id) {
        Permission perm = new Permission();
        perm.setId(id);
        perm.setPackages(new HashSet<>()); // Khởi tạo để tránh NPE
        return perm;
    }

    private PackageResponse createPackageResponse(Long id, String packName) {
        PackageResponse response = new PackageResponse();
        response.setId(id);
        response.setPackName(packName);
        return response;
    }
}

