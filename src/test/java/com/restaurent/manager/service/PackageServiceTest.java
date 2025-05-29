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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PackageServiceTest {

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PackageMapper packageMapper;

    // Phương thức chạy trước mỗi test case để đảm bảo trạng thái sạch sẽ
    @BeforeEach
    void setup() {
        // Clean up database before each test
        packageRepository.deleteAll();
        permissionRepository.deleteAll();
        restaurantRepository.deleteAll();
    }

    // Test case 1: Trường hợp bình thường với yêu cầu hợp lệ và quyền tồn tại
    // ID: PkS-1
    @Test
    void testCreate_NormalCase() {
        // Arrange (chuẩn bị dữ liệu)
        String packName = "testPackage";
        Set<Long> permissionIds = new HashSet<>();

        // Create permissions first in database
        Permission permission1 = new Permission();
        permission1.setName("PERMISSION_1");
        permission1 = permissionRepository.save(permission1);

        Permission permission2 = new Permission();
        permission2.setName("PERMISSION_2");
        permission2 = permissionRepository.save(permission2);

        permissionIds.add(permission1.getId());
        permissionIds.add(permission2.getId());

        PackageRequest request = createRequest(packName, permissionIds);

        // Act (thực thi phương thức cần kiểm thử)
        PackageResponse response = packageService.create(request);

        // Assert (kiểm tra kết quả)
        // Kiểm tra phản hồi không null và chứa thông tin đúng
        assertNotNull(response);
        assertEquals("TESTPACKAGE", response.getPackName());

        // Verify in database - kiểm tra gói đã được lưu đúng trong database
        Package savedPackage = packageRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedPackage);
        assertEquals("TESTPACKAGE", savedPackage.getPackName());
        assertEquals(2, savedPackage.getPermissions().size());

        // Kiểm tra quyền đã được liên kết đúng với gói
        assertTrue(savedPackage.getPermissions().contains(permission1));
        assertTrue(savedPackage.getPermissions().contains(permission2));
    }

    // Test case 2: Trường hợp danh sách quyền rỗng
    // ID: PkS-2
    @Test
    void testCreate_NoPermissions() {
        // Arrange
        // Tạo request với danh sách quyền rỗng
        PackageRequest request = createRequest("testPackage", Collections.emptySet());

        // Act
        PackageResponse response = packageService.create(request);

        // Assert
        // Kiểm tra tên gói vẫn được chuyển thành chữ in hoa
        assertNotNull(response);
        assertEquals("TESTPACKAGE", response.getPackName());

        // Verify in database - kiểm tra không có quyền nào được liên kết
        Package savedPackage = packageRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedPackage);
        assertEquals("TESTPACKAGE", savedPackage.getPackName());
        assertNull(savedPackage.getPermissions());
    }

    // Test case 3: Trường hợp một số quyền không tồn tại
    // ID: PkS-3
    @Test
    void testCreate_InvalidPermissionIds() {
        // Arrange
        // Tạo request với danh sách quyền, trong đó một số không tồn tại
        Permission existingPermission = new Permission();
        existingPermission.setName("EXISTING_PERMISSION");
        existingPermission = permissionRepository.save(existingPermission);

        Set<Long> permissionIds = Set.of(existingPermission.getId(), 999L); // 999L không tồn tại
        PackageRequest request = createRequest("testPackage", permissionIds);

        // Act
        PackageResponse response = packageService.create(request);

        // Assert
        // Kiểm tra chỉ quyền tồn tại được liên kết
        assertNotNull(response);
        assertEquals("TESTPACKAGE", response.getPackName());

        // Verify in database
        Package savedPackage = packageRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedPackage);
        assertEquals(1, savedPackage.getPermissions().size());
        assertTrue(savedPackage.getPermissions().contains(existingPermission));
    }

    // Test case 4: Trường hợp danh sách quyền null
    // ID: PkS-4
    @Test
    void testCreate_NullPermissions() {
        // Arrange
        // Tạo request với permissions null
        PackageRequest request = createRequest("testPackage", null);

        // Act & Assert
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            packageService.create(request);
        });
    }

    // Test case 5: Trường hợp tên gói null (kiểm tra ngoại lệ)
    // ID: PkS-5
    @Test
    void testCreate_NullPackName() {
        // Arrange
        PackageRequest request = createRequest(null, Collections.emptySet());

        // Act & Assert
        // Kiểm tra xem phương thức ném NullPointerException khi gọi toUpperCase trên null
        assertThrows(NullPointerException.class, () -> packageService.create(request));
    }

    // Test case 1: Kiểm tra khi danh sách Package không rỗng
    // ID: PkS-6
    @Test
    void testGetPacks_NonEmptyList() {
        // Arrange (Chuẩn bị dữ liệu)
        // Tạo và lưu Package vào database
        Package pack1 = createPackage(null, "BASIC");
        Package pack2 = createPackage(null, "PREMIUM");
        pack1 = packageRepository.save(pack1);
        pack2 = packageRepository.save(pack2);

        // Act (Thực thi phương thức cần kiểm thử)
        List<PackageResponse> result = packageService.getPacks();

        // Assert (Kiểm tra kết quả)
        // Kiểm tra kích thước danh sách trả về
        assertNotNull(result);
        assertEquals(2, result.size());

        // Kiểm tra chi tiết từng PackageResponse trong kết quả
        PackageResponse result1 = result.get(0);
        PackageResponse result2 = result.get(1);

        // Verify package names exist in results
        assertTrue(result.stream().anyMatch(p -> "BASIC".equals(p.getPackName())));
        assertTrue(result.stream().anyMatch(p -> "PREMIUM".equals(p.getPackName())));
    }

    // Test case 2: Kiểm tra khi danh sách Package rỗng
    // ID: PkS-7
    @Test
    void testGetPacks_EmptyList() {
        packageRepository.deleteAll();
        // Act
        List<PackageResponse> result = packageService.getPacks();

        // Assert
        // Kiểm tra danh sách trả về rỗng
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Test case 1: Kiểm tra trường hợp thành công khi cả Permission và Package tồn tại
    // ID: PkS-8
    @Test
    void testAddPermission_Success() {
        // Arrange (Chuẩn bị dữ liệu)
        // Tạo và lưu Package vào database
        Package pack = createPackage(null, "TEST_PACK");
        pack = packageRepository.save(pack);

        // Tạo và lưu Permission vào database
        Permission permission = new Permission();
        permission.setName("NEW_PERMISSION");
        permission = permissionRepository.save(permission);

        // Act (Thực thi phương thức cần kiểm thử)
        PackageResponse result = packageService.addPermission(permission.getId(), pack.getId());

        // Assert (Kiểm tra kết quả)
        assertNotNull(result);
        assertEquals("TEST_PACK", result.getPackName());

        // Verify in database - kiểm tra permission đã được thêm vào package
        Package updatedPackage = packageRepository.findById(pack.getId()).orElse(null);
        assertNotNull(updatedPackage);
        assertEquals(1, updatedPackage.getPermissions().size());
        assertTrue(updatedPackage.getPermissions().contains(permission));
    }

    // Test case 2: Kiểm tra khi Permission không tồn tại
    // ID: PkS-9
    @Test
    void testAddPermission_PermissionNotFound() {
        // Arrange
        Package pack = createPackage(null, "TEST_PACK");
        pack = packageRepository.save(pack);

        Long nonExistentPermissionId = 999L;
        final Long packId = pack.getId();

        // Act & Assert (Thực thi và kiểm tra ngoại lệ)
        AppException exception = assertThrows(AppException.class, () ->
                packageService.addPermission(nonExistentPermissionId, packId)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.INVALID_KEY, exception.getErrorCode());
    }

    // Test case 3: Kiểm tra khi Package không tồn tại
    // ID: PkS-10
    @Test
    void testAddPermission_PackageNotFound() {
        // Arrange
        Permission permission = new Permission();
        permission.setName("PERMISSION");
        permission = permissionRepository.save(permission);

        Long nonExistentPackageId = 999L;
        final Long permissionId = permission.getId();

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.addPermission(permissionId, nonExistentPackageId)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.INVALID_KEY, exception.getErrorCode());
    }

    // Test case 1: Thành công với danh sách quyền không rỗng
    // ID: PkS-11
    @Test
    void testUpdatePackage_SuccessWithPermissions() {
        // Arrange (Chuẩn bị dữ liệu)
        Package pack = createPackage(null, "OriginalPack");
        pack = packageRepository.save(pack);

        Permission permission1 = new Permission();
        permission1.setName("PERMISSION_1");
        permission1 = permissionRepository.save(permission1);

        Permission permission2 = new Permission();
        permission2.setName("PERMISSION_2");
        permission2 = permissionRepository.save(permission2);

        Set<Long> permissionIds = Set.of(permission1.getId(), permission2.getId());
        PackageRequest request = createRequest("UpdatedPack", permissionIds);

        // Act (Thực thi phương thức cần kiểm thử)
        PackageResponse result = packageService.updatePackage(pack.getId(), request);

        // Assert (Kiểm tra kết quả)
        assertNotNull(result);
        assertEquals("UpdatedPack", result.getPackName());

        // Verify in database - kiểm tra package đã được cập nhật
        Package updatedPackage = packageRepository.findById(pack.getId()).orElse(null);
        assertNotNull(updatedPackage);
        assertEquals("UpdatedPack", updatedPackage.getPackName());
        assertEquals(2, updatedPackage.getPermissions().size());
        assertTrue(updatedPackage.getPermissions().contains(permission1));
        assertTrue(updatedPackage.getPermissions().contains(permission2));
    }

    // Test case 2: Thành công với danh sách quyền rỗng
    // ID: PkS-12
    @Test
    void testUpdatePackage_EmptyPermissions() {
        // Arrange
        Package pack = createPackage(null, "OriginalPack");
        pack = packageRepository.save(pack);

        PackageRequest request = createRequest("UpdatedPack", Collections.emptySet());

        // Act
        PackageResponse result = packageService.updatePackage(pack.getId(), request);

        // Assert
        assertNotNull(result);
        assertEquals("UpdatedPack", result.getPackName());

        // Verify in database - kiểm tra permissions không bị thay đổi (vẫn là tập rỗng)
        Package updatedPackage = packageRepository.findById(pack.getId()).orElse(null);
        assertNotNull(updatedPackage);
        assertEquals("UpdatedPack", updatedPackage.getPackName());
        assertTrue(updatedPackage.getPermissions().isEmpty());
    }

    // Test case 3: Package không tồn tại
    // ID: PkS-13
    @Test
    void testUpdatePackage_PackageNotFound() {
        // Arrange
        Long nonExistentPackageId = 999L;
        PackageRequest request = createRequest("UpdatedPack", Set.of(1L, 2L));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.updatePackage(nonExistentPackageId, request)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    // Test case 1: Thành công khi Package tồn tại
    // ID: PkS-14
    @Test
    void testFindPackById_Success() {
        // Arrange (Chuẩn bị dữ liệu)
        Package pack = createPackage(null, "TestPack");
        pack = packageRepository.save(pack);

        // Act (Thực thi phương thức cần kiểm thử)
        Package result = packageService.findPackById(pack.getId());

        // Assert (Kiểm tra kết quả)
        // Kiểm tra kết quả trả về không null và đúng Package mong đợi
        assertNotNull(result);
        assertEquals(pack.getId(), result.getId());
        assertEquals("TestPack", result.getPackName());
    }

    // Test case 2: Thất bại khi Package không tồn tại
    // ID: PkS-15
    @Test
    void testFindPackById_PackageNotFound() {
        // Arrange
        Long nonExistentPackageId = 999L;

        // Act & Assert (Thực thi và kiểm tra ngoại lệ)
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findPackById(nonExistentPackageId)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    // Test case 1: Thành công khi Package tồn tại
    // ID: PkS-16
    @Test
    void testFindByPackName_Success() {
        // Arrange (Chuẩn bị dữ liệu)
        String packName = "TestPack";
        Package pack = createPackage(null, packName);
        packageRepository.save(pack);

        // Act (Thực thi phương thức cần kiểm thử)
        Package result = packageService.findByPackName(packName);

        // Assert (Kiểm tra kết quả)
        // Kiểm tra kết quả trả về không null và đúng Package mong đợi
        assertNotNull(result);
        assertEquals(packName, result.getPackName());
    }

    // Test case 2: Thất bại khi Package không tồn tại
    // ID: PkS-17
    @Test
    void testFindByPackName_PackageNotFound() {
        // Arrange
        String nonExistentPackName = "NonExistentPack";

        // Act & Assert (Thực thi và kiểm tra ngoại lệ)
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findByPackName(nonExistentPackName)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.PACKAGE_NOT_EXIST, exception.getErrorCode());
    }

    // Test case 3: PackageName là null, giả định repository trả về Optional.empty()
    // ID: PkS-18
    @Test
    void testFindByPackName_NullName_ReturnsEmpty() {
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findByPackName(null)
        );

        // Xác minh ngoại lệ đúng loại và mã lỗi
        assertEquals(ErrorCode.PACKAGE_NOT_EXIST, exception.getErrorCode());
    }

    // Test case 1: Thành công với daysLeft > 0
    // ID: PkS-19
    @Test
    void testFindPacksToUpgradeForRestaurant_Success_DaysLeftPositive() {
        // Arrange
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        Package currentPack = createPackage(null, "Basic", 100.0);
        currentPack = packageRepository.save(currentPack);

        Package upgradePack = createPackage(null, "Premium", 200.0);
        upgradePack = packageRepository.save(upgradePack);

        restaurant.setRestaurantPackage(currentPack);
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(10));
        restaurantRepository.save(restaurant);

        // Act
        PackUpgradeResponse result = packageService.findPacksToUpgradeForRestaurant(restaurant.getId());

        // Assert
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
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant = restaurantRepository.save(restaurant);

        Package currentPack = createPackage(null, "Basic", 100.0);
        currentPack = packageRepository.save(currentPack);

        Package upgradePack = createPackage(null, "Premium", 200.0);
        upgradePack = packageRepository.save(upgradePack);

        restaurant.setRestaurantPackage(currentPack);
        restaurant.setExpiryDate(LocalDateTime.now().minusDays(1));
        restaurantRepository.save(restaurant);

        // Act
        PackUpgradeResponse result = packageService.findPacksToUpgradeForRestaurant(restaurant.getId());

        // Assert
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
        Long nonExistentRestaurantId = 999L;

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                packageService.findPacksToUpgradeForRestaurant(nonExistentRestaurantId)
        );

        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    // Test case 1: Danh sách Package không rỗng
    // ID: PkS-22
    @Test
    void testGetPacksView_NonEmptyList() {
        // Arrange (Chuẩn bị dữ liệu)
        Package pack1 = createPackage(null, "Basic");
        Package pack2 = createPackage(null, "Premium");
        packageRepository.save(pack1);
        packageRepository.save(pack2);

        // Act (Thực thi phương thức cần kiểm thử)
        List<PackageResponse> result = packageService.getPacksView();

        // Assert (Kiểm tra kết quả)
        assertNotNull(result);
        assertEquals(2, result.size());

        // Kiểm tra kích thước và nội dung danh sách trả về
        assertTrue(result.stream().anyMatch(p -> "Basic".equals(p.getPackName())));
        assertTrue(result.stream().anyMatch(p -> "Premium".equals(p.getPackName())));
    }

    // Test case 2: Danh sách Package rỗng
    // ID: PkS-23
    @Test
    void testGetPacksView_EmptyList() {
        packageRepository.deleteAll();

        // Act
        List<PackageResponse> result = packageService.getPacksView();

        // Assert
        // Kiểm tra danh sách trả về rỗng
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Các phương thức hỗ trợ tạo đối tượng
    private PackageRequest createRequest(String packName, Set<Long> permissionIds) {
        PackageRequest request = new PackageRequest();
        request.setPackName(packName);
        request.setPermissions(permissionIds);
        return request;
    }

    private Package createPackage(Long id, String packName) {
        Package pack = new Package();
        if (id != null) {
            pack.setId(id);
        }
        pack.setPackName(packName);
        pack.setPermissions(new HashSet<>()); // Khởi tạo để tránh NPE
        return pack;
    }

    private Package createPackage(Long id, String packName, double pricePerMonth) {
        Package pack = new Package();
        if (id != null) {
            pack.setId(id);
        }
        pack.setPackName(packName);
        pack.setPermissions(new HashSet<>()); // Khởi tạo để tránh NPE
        pack.setPricePerMonth(pricePerMonth);
        return pack;
    }
}

