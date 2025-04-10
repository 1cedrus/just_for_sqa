package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.RestaurantPackagePaymentHistoryRequest;
import com.restaurent.manager.dto.request.restaurant.RestaurantUpdateRequest;
import com.restaurent.manager.dto.response.StatisticAdminTable;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.RestaurantPackagePaymentHistory;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.mapper.RestaurantPackageHistoryMapper;
import com.restaurent.manager.repository.RestaurantPackagePaymentHistoryRepository;
import com.restaurent.manager.service.impl.AccountService;
import com.restaurent.manager.service.impl.RestaurantPackagePaymentHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho RestaurantPackagePaymentHistoryService
 * Sử dụng Mockito để mock các dependency như repository, service, và mapper
 * Mục tiêu: Đạt branch coverage khoảng 80% cho tất cả các phương thức
 * Các test tập trung vào kiểm tra logic chính và các nhánh quan trọng
 */
class RestaurantPackagePaymentHistoryServiceTest {

    @Mock
    private RestaurantPackagePaymentHistoryRepository restaurantPackagePaymentHistoryRepository; // Mock repository để lưu trữ lịch sử thanh toán

    @Mock
    private IPackageService packageService; // Mock service để tìm package

    @Mock
    private IRestaurantService restaurantService; // Mock service để quản lý restaurant

    @Mock
    private RestaurantPackageHistoryMapper mapper; // Mock mapper để chuyển đổi request sang entity

    @Mock
    private AccountService accountService; // Mock service để quản lý account

    @Mock
    private IEmailService emailService; // Mock service để gửi email

    @InjectMocks
    private RestaurantPackagePaymentHistoryService service; // Service cần test, inject các mock vào

    /**
     * Thiết lập trước mỗi test case
     * Khởi tạo các mock objects để đảm bảo môi trường test sạch sẽ
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo tất cả các mock được đánh dấu @Mock
    }

    // --- Tests cho createRestaurantPackagePaymentHistory ---
    /**
     * Test tạo một RestaurantPackagePaymentHistory thành công
     * Kiểm tra xem phương thức có tạo và lưu lịch sử thanh toán đúng với request không
     */
    @Test
    void testCreateRestaurantPackagePaymentHistory_SuccessWithExistHistory() {
        // Chuẩn bị dữ liệu test
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(1L, 1L, 1L, 100.0D, 12); // Request với packageId, restaurantId, accountId
        RestaurantPackagePaymentHistory history = createRestaurantPackagePaymentHistory(1L, 1L, 100.0D, 12); // Entity lịch sử thanh toán
        RestaurantPackagePaymentHistory existed = createRestaurantPackagePaymentHistory(2L, 1L, 1L, 100.0D, 12);

        // Mock hành vi của các dependency
        when(mapper.toRestaurantPackagePaymentHistory(request)).thenReturn(history); // Mapper chuyển request thành entity
        when(packageService.findPackById(1L)).thenReturn(new Package()); // Tìm package thành công
        when(restaurantService.getRestaurantById(1L)).thenReturn(new Restaurant()); // Tìm restaurant thành công
        when(restaurantPackagePaymentHistoryRepository.findAll()).thenReturn(List.of(existed)); // Danh sách rỗng -> ID mới = 1
        when(restaurantPackagePaymentHistoryRepository.save(any())).thenReturn(history); // Lưu entity và trả về

        // Thực thi phương thức cần test
        Long result = service.createRestaurantPackagePaymentHistory(request);

        // Kiểm tra kết quả
        assertEquals(3L, result); // ID trả về phải là 1
        verify(mapper, times(1)).toRestaurantPackagePaymentHistory(request); // Xác minh mapper được gọi 1 lần
        verify(packageService, times(1)).findPackById(1L); // Xác minh tìm package 1 lần
        verify(restaurantService, times(1)).getRestaurantById(1L); // Xác minh tìm restaurant 1 lần
        verify(restaurantPackagePaymentHistoryRepository, times(1)).save(history); // Xác minh lưu entity 1 lần
    }

    /**
     * Test tạo một RestaurantPackagePaymentHistory thành công
     * Kiểm tra xem phương thức có tạo và lưu lịch sử thanh toán đúng với request không
     */
    @Test
    void testCreateRestaurantPackagePaymentHistory_Success() {
        // Chuẩn bị dữ liệu test
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(1L, 1L, 1L, 100.0D, 12); // Request với packageId, restaurantId, accountId
        RestaurantPackagePaymentHistory history = createRestaurantPackagePaymentHistory(1L, 1L, 100.0D, 12); // Entity lịch sử thanh toán

        // Mock hành vi của các dependency
        when(mapper.toRestaurantPackagePaymentHistory(request)).thenReturn(history); // Mapper chuyển request thành entity
        when(packageService.findPackById(1L)).thenReturn(new Package()); // Tìm package thành công
        when(restaurantService.getRestaurantById(1L)).thenReturn(new Restaurant()); // Tìm restaurant thành công
        when(restaurantPackagePaymentHistoryRepository.findAll()).thenReturn(Collections.emptyList()); // Danh sách rỗng -> ID mới = 1
        when(restaurantPackagePaymentHistoryRepository.save(any())).thenReturn(history); // Lưu entity và trả về

        // Thực thi phương thức cần test
        Long result = service.createRestaurantPackagePaymentHistory(request);

        // Kiểm tra kết quả
        assertEquals(1L, result); // ID trả về phải là 1
        verify(mapper, times(1)).toRestaurantPackagePaymentHistory(request); // Xác minh mapper được gọi 1 lần
        verify(packageService, times(1)).findPackById(1L); // Xác minh tìm package 1 lần
        verify(restaurantService, times(1)).getRestaurantById(1L); // Xác minh tìm restaurant 1 lần
        verify(restaurantPackagePaymentHistoryRepository, times(1)).save(history); // Xác minh lưu entity 1 lần
    }
    // --- Tests cho getNewId ---
    /**
     * Test lấy ID mới khi danh sách lịch sử rỗng
     * Kiểm tra xem ID trả về có phải là 1 khi không có bản ghi nào không
     */
    @Test
    void testGetNewId_EmptyList() {
        // Mock hành vi: repository trả về danh sách rỗng
        when(restaurantPackagePaymentHistoryRepository.findAll()).thenReturn(Collections.emptyList());

        // Thực thi phương thức
        Long result = service.getNewId();

        // Kiểm tra kết quả
        assertEquals(1L, result); // Khi danh sách rỗng, ID mới phải là 1
        verify(restaurantPackagePaymentHistoryRepository, times(1)).findAll(); // Xác minh gọi findAll 1 lần
    }

    /**
     * Test lấy ID mới khi đã có dữ liệu
     * Kiểm tra xem ID mới có được tăng lên từ ID cuối cùng không
     */
    @Test
    void testGetNewId_WithData() {
        // Chuẩn bị dữ liệu: danh sách có 1 bản ghi với ID = 5
        List<RestaurantPackagePaymentHistory> histories = List.of(
                createRestaurantPackagePaymentHistory(5L, 1L, 1L, 100.0, 12)
        );
        when(restaurantPackagePaymentHistoryRepository.findAll()).thenReturn(histories); // Mock repository trả về danh sách

        // Thực thi
        Long result = service.getNewId();

        // Kiểm tra
        assertEquals(6L, result); // ID mới phải là 5 + 1 = 6
        verify(restaurantPackagePaymentHistoryRepository, times(1)).findAll(); // Xác minh gọi findAll 1 lần
    }

    // --- Tests cho updateRestaurantPackagePaymentHistory ---
    /**
     * Test cập nhật lịch sử thanh toán thành công
     * Kiểm tra xem trạng thái paid được cập nhật và email được gửi không
     */
    @Test
    void testUpdateRestaurantPackagePaymentHistory_Success() {
        // Chuẩn bị dữ liệu
        Long id = 1L;
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(1L, 1L, 3L, 100.0D, 12); // Request với packageId, restaurantId, accountId, months
        RestaurantPackagePaymentHistory history = createRestaurantPackagePaymentHistory(1L, 2L, 3L, 100.0, 12); // Entity ban đầu
        Package pack = createPackage("Basic"); // Package mẫu
        Account account = createAccount("test@example.com"); // Account mẫu
        String token = "token123"; // Token mẫu

        // Mock hành vi
        when(restaurantPackagePaymentHistoryRepository.findById(id)).thenReturn(Optional.of(history)); // Tìm thấy history
        when(restaurantService.updateRestaurant(eq(3L), any(RestaurantUpdateRequest.class))).thenReturn(any()); // Cập nhật restaurant
        when(packageService.findPackById(1L)).thenReturn(pack); // Tìm package
        when(accountService.findAccountByID(1L)).thenReturn(account); // Tìm account
        when(accountService.generateToken(account)).thenReturn(token); // Tạo token
        when(restaurantPackagePaymentHistoryRepository.save(history)).thenReturn(history); // Lưu history

        // Thực thi
        String result = service.updateRestaurantPackagePaymentHistory(id, request);

        // Kiểm tra
        assertEquals(token, result); // Token trả về phải khớp
        assertTrue(history.isPaid()); // Trạng thái paid phải là true
        verify(restaurantService, times(1)).updateRestaurant(eq(3L), any(RestaurantUpdateRequest.class)); // Xác minh cập nhật restaurant
        verify(emailService, times(1)).sendEmail(eq("test@example.com"), any(), any()); // Xác minh gửi email
    }

    /**
     * Test cập nhật với ID không tồn tại
     * Kiểm tra xem exception có được ném ra khi không tìm thấy history không
     */
    @Test
    void testUpdateRestaurantPackagePaymentHistory_NotFound() {
        // Chuẩn bị dữ liệu
        Long id = 1L;
        RestaurantPackagePaymentHistoryRequest request = createRestaurantPackagePaymentHistoryRequest(1L, 2L, 3L, 100.0D, 12);

        // Mock hành vi: không tìm thấy history
        when(restaurantPackagePaymentHistoryRepository.findById(id)).thenReturn(Optional.empty());

        // Thực thi và mong đợi exception
        assertThrows(AppException.class, () -> service.updateRestaurantPackagePaymentHistory(id, request));

        // Xác minh không có tương tác thêm với các service khác
        verifyNoMoreInteractions(restaurantService, packageService, accountService, emailService);
    }

    // --- Tests cho getTotalValueByDate ---
    /**
     * Test lấy tổng giá trị với code hợp lệ
     * Kiểm tra xem phương thức có trả về danh sách khi code đúng không
     */
    @Test
    void testGetTotalValueByDate_ValidCode() {
        // Chuẩn bị dữ liệu
        String code = "current-week";
        // Mock để tránh getNewId() ảnh hưởng (do gọi findAll)
        when(restaurantPackagePaymentHistoryRepository.findAll()).thenReturn(Collections.emptyList());

        // Thực thi
        List<StatisticAdminTable> result = service.getTotalValueByDate(code);

        // Kiểm tra
        assertNotNull(result); // Kết quả không được null khi code hợp lệ
        assertFalse(result.isEmpty()); // Danh sách phải có dữ liệu (tuần có 7 ngày)
    }

    /**
     * Test lấy tổng giá trị với code không hợp lệ
     * Kiểm tra xem phương thức trả về null khi code không khớp
     */
    @Test
    void testGetTotalValueByDate_InvalidCode() {
        // Chuẩn bị dữ liệu
        String code = "invalid";

        // Thực thi
        List<StatisticAdminTable> result = service.getTotalValueByDate(code);

        // Kiểm tra
        assertNull(result); // Mong đợi null khi code không hợp lệ
    }

    // --- Tests cho getProfitInCurrentMonth ---
    /**
     * Test lấy lợi nhuận tháng hiện tại
     * Kiểm tra xem danh sách trả về có đúng số ngày từ đầu tháng đến hiện tại không
     */
    @Test
    void testGetProfitInCurrentMonth() {
        // Mock hành vi của các dependency
        when(restaurantService.countRestaurantByDateCreated(any())).thenReturn(5); // Số restaurant mỗi ngày
        when(restaurantPackagePaymentHistoryRepository.findByDateCreated(any())).thenReturn(Collections.emptyList()); // Không có history

        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInCurrentMonth();

        // Kiểm tra
        int currentDay = LocalDateTime.now().getDayOfMonth();
        assertEquals(currentDay, result.size()); // Số ngày từ đầu tháng đến hiện tại
        assertEquals(5, result.getFirst().getTotalRestaurant()); // Số restaurant mỗi ngày
        assertEquals(0.0D, result.getFirst().getTotal()); // Tổng giá trị mỗi ngày
    }

    // --- Tests cho getProfitInLastMonth ---
    /**
     * Test lấy lợi nhuận tháng trước
     * Kiểm tra xem danh sách trả về có đúng số ngày của tháng trước không
     */
    @Test
    void testGetProfitInLastMonth() {
        // Mock hành vi
        when(restaurantService.countRestaurantByDateCreated(any())).thenReturn(3); // Số restaurant mỗi ngày
        when(restaurantPackagePaymentHistoryRepository.findByDateCreated(any())).thenReturn(Collections.emptyList()); // Không có history

        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInLastMonth();

        // Kiểm tra
        int daysInLastMonth = LocalDate.now().minusMonths(1).lengthOfMonth();
        assertEquals(daysInLastMonth, result.size()); // Số ngày trong tháng trước
        assertEquals(3, result.getFirst().getTotalRestaurant()); // Số restaurant mỗi ngày
        assertEquals(0.0D, result.getFirst().getTotal()); // Tổng giá trị mỗi ngày
    }

    // --- Tests cho getProfitInCurrentWeek ---
    /**
     * Test lấy lợi nhuận tuần hiện tại
     * Kiểm tra xem danh sách trả về có đúng 7 ngày của tuần hiện tại không
     */
    @Test
    void testGetProfitInCurrentWeek() {
        // Mock hành vi
        when(restaurantService.countRestaurantByDateCreated(any())).thenReturn(4); // Số restaurant mỗi ngày
        when(restaurantPackagePaymentHistoryRepository.findByDateCreated(any())).thenReturn(Collections.emptyList()); // Không có history

        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInCurrentWeek();

        // Kiểm tra
        assertEquals(7, result.size()); // Tuần có 7 ngày
        assertEquals(4, result.getFirst().getTotalRestaurant()); // Số restaurant mỗi ngày
        assertEquals(0.0D, result.getFirst().getTotal()); // Tổng giá trị mỗi ngày
    }

    // --- Tests cho getProfitInLastWeek ---
    /**
     * Test lấy lợi nhuận tuần trước
     * Kiểm tra xem danh sách trả về có đúng 7 ngày của tuần trước không
     */
    @Test
    void testGetProfitInLastWeek() {
        // Mock hành vi
        when(restaurantService.countRestaurantByDateCreated(any())).thenReturn(2); // Số restaurant mỗi ngày
        when(restaurantPackagePaymentHistoryRepository.findByDateCreated(any())).thenReturn(Collections.emptyList()); // Không có history

        // Thực thi
        List<StatisticAdminTable> result = service.getProfitInLastWeek();

        // Kiểm tra
        assertEquals(7, result.size()); // Tuần có 7 ngày
        assertEquals(2, result.getFirst().getTotalRestaurant()); // Số restaurant mỗi ngày
        assertEquals(0.0D, result.getFirst().getTotal()); // Tổng giá trị mỗi ngày
    }

    // --- Tests cho totalValueInDate ---
    /**
     * Test tính tổng giá trị trong ngày - có dữ liệu
     * Kiểm tra xem tổng chỉ tính các history đã paid không
     */
    @Test
    void testTotalValueInDate_WithData() {
        // Chuẩn bị dữ liệu
        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        List<RestaurantPackagePaymentHistory> histories = List.of(
                createRestaurantPackagePaymentHistory(1L, 1L, 1000.0, 12, true), // Đã thanh toán
                createRestaurantPackagePaymentHistory(2L, 2L, 500.0, 6, false)   // Chưa thanh toán
        );
        when(restaurantPackagePaymentHistoryRepository.findByDateCreated(sqlDate)).thenReturn(histories); // Mock dữ liệu history

        // Thực thi
        double result = service.totalValueInDate(date);

        // Kiểm tra
        assertEquals(1000, result); // Chỉ tính history đã paid (1000.0)
        verify(restaurantPackagePaymentHistoryRepository, times(1)).findByDateCreated(sqlDate); // Xác minh gọi repository
    }

    /**
     * Test tính tổng giá trị trong ngày - không có dữ liệu
     * Kiểm tra xem trả về 0 khi không có history nào
     */
    @Test
    void testTotalValueInDate_NoData() {
        // Chuẩn bị dữ liệu
        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = Date.valueOf(date);
        when(restaurantPackagePaymentHistoryRepository.findByDateCreated(sqlDate)).thenReturn(Collections.emptyList()); // Mock danh sách rỗng

        // Thực thi
        double result = service.totalValueInDate(date);

        // Kiểm tra
        assertEquals(0.0, result); // Không có dữ liệu -> trả về 0
        verify(restaurantPackagePaymentHistoryRepository, times(1)).findByDateCreated(sqlDate); // Xác minh gọi repository
    }

    private RestaurantPackagePaymentHistoryRequest createRestaurantPackagePaymentHistoryRequest(
            Long accountId,
            Long packageId,
            Long restaurantId,
            double totalMoney,
            int month
    ) {
        RestaurantPackagePaymentHistoryRequest request = new RestaurantPackagePaymentHistoryRequest();
        request.setAccountId(accountId);
        request.setPackageId(packageId);
        request.setRestaurantId(restaurantId);
        request.setTotalMoney(totalMoney);
        request.setMonths(month);
        return request;
    }

    private RestaurantPackagePaymentHistory createRestaurantPackagePaymentHistory(
            Long packageId,
            Long restaurantId,
            double totalMoney,
            int month
    ) {
       return RestaurantPackagePaymentHistory.builder()
               .packageId(packageId)
               .restaurantId(restaurantId)
               .months(month)
               .totalMoney(totalMoney)
               .build();
    }


    private RestaurantPackagePaymentHistory createRestaurantPackagePaymentHistory(
            Long packageId,
            Long restaurantId,
            double totalMoney,
            int month,
            boolean isPaid
    ) {
        return RestaurantPackagePaymentHistory.builder()
                .packageId(packageId)
                .restaurantId(restaurantId)
                .months(month)
                .totalMoney(totalMoney)
                .isPaid(isPaid)
                .build();
    }

    private RestaurantPackagePaymentHistory createRestaurantPackagePaymentHistory(
            Long id,
            Long packageId,
            Long restaurantId,
            double totalMoney,
            int month
    ) {
        return RestaurantPackagePaymentHistory.builder()
                .id(id)
                .packageId(packageId)
                .restaurantId(restaurantId)
                .months(month)
                .totalMoney(totalMoney)
                .build();
    }

    private Package createPackage(String name) {
        return Package.builder()
                .packName(name)
                .build();
    }

    private Account createAccount(String email) {
        return Account.builder()
                .email(email)
                .build();
    }
}