package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishCategoryRequest;
import com.restaurent.manager.dto.response.DishCategoryResponse;
import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishCategoryMapper;
import com.restaurent.manager.repository.DishCategoryRepository;
import com.restaurent.manager.service.IRestaurantService;
import com.restaurent.manager.service.impl.DishCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class) // Tích hợp Mockito với JUnit 5
@Transactional
public class DishCategoryServiceTest {

    // Mock các dependency của DishCategoryService
    @Mock
    private DishCategoryRepository dishCategoryRepository;

    @Mock
    private DishCategoryMapper dishCategoryMapper;

    @Mock
    private IRestaurantService restaurantService;

    // Inject các mock vào DishCategoryService (tạo instance với các mock)
    @InjectMocks
    private DishCategoryService dishCategoryService;

    // Biến mẫu để tái sử dụng trong các test case
    private DishCategory sampleCategory;
    private DishCategoryRequest sampleRequest;
    private DishCategoryResponse sampleResponse;
    private Restaurant sampleRestaurant;

    /**
     * Thiết lập dữ liệu mẫu trước mỗi test case.
     * Được gọi trước mỗi @Test để khởi tạo các đối tượng mẫu và reset trạng thái mock.
     */
    @BeforeEach
    public void setUp() {
        // Tạo DishCategory mẫu
        sampleCategory = new DishCategory();
        sampleCategory.setId(1L);
        sampleCategory.setName("Drinks");
        sampleCategory.setCode("drinks");

        // Tạo DishCategoryRequest mẫu (cho create)
        sampleRequest = new DishCategoryRequest();
        sampleRequest.setName("Drinks");
        sampleRequest.setRestaurantId(1L);

        // Tạo DishCategoryResponse mẫu
        sampleResponse = DishCategoryResponse.builder().id(1L).name("Drinks").build();

        // Tạo Restaurant mẫu
        sampleRestaurant = new Restaurant(); // Giả định Restaurant có constructor với ID
        sampleRestaurant.setId(1L);
    }

    /**
     * Test createDishCategory khi tạo thành công (category chưa tồn tại).
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testCreateDishCategory_Success() {
        // Mock
        when(dishCategoryRepository.existsByNameAndRestaurant_Id("Drinks", 1L)).thenReturn(false);
        when(dishCategoryMapper.toDishCategory(sampleRequest)).thenReturn(sampleCategory);
        when(restaurantService.getRestaurantById(1L)).thenReturn(sampleRestaurant);
        when(dishCategoryRepository.save(any(DishCategory.class))).thenReturn(sampleCategory);
        when(dishCategoryMapper.toDishCategoryResponse(sampleCategory)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        DishCategoryResponse response = dishCategoryService.createDishCategory(sampleRequest);

        // Xác nhận kết quả
        assertNotNull(response, "Response không được null");
        assertEquals("Drinks", response.getName(), "Tên category phải là 'Drinks'");
        // Xác nhận save được gọi với category có code được tạo từ SlugUtils
        verify(dishCategoryRepository).save(argThat(category -> category.getCode().equals("drinks")));
    }

    /**
     * Test createDishCategory khi category đã tồn tại (ném exception).
     * Kiểm tra nhánh thất bại với AppException.
     */
    @Test
    public void testCreateDishCategory_DuplicateName() {
        // Mock hành vi: category đã tồn tại
        when(dishCategoryRepository.existsByNameAndRestaurant_Id("Drinks", 1L)).thenReturn(true);

        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            dishCategoryService.createDishCategory(sampleRequest);
        });

        // Xác nhận lỗi là DISH_CATEGORY_EXIST
        assertEquals(ErrorCode.DISH_CATEGORY_EXIST, exception.getErrorCode(), "Lỗi phải là DISH_CATEGORY_EXIST");
        // Xác nhận không gọi save khi category đã tồn tại
        verify(dishCategoryRepository, never()).save(any());
    }

    /**
     * Test getAllDishCategoryByRestaurantId khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công với phân trang.
     */
    @Test
    public void testGetAllDishCategoryByRestaurantId() {
        // Tạo Pageable mẫu
        Pageable pageable = PageRequest.of(0, 10);
        // Mock danh sách category trả về
        List<DishCategory> categories = List.of(sampleCategory);
        when(dishCategoryRepository.findByRestaurant_IdAndNameContaining(1L, "Drink", pageable)).thenReturn(categories);
        // Mock đếm số lượng
        when(dishCategoryRepository.countByRestaurant_IdAndNameContaining(1L, "Drink")).thenReturn(1);
        // Mock ánh xạ
        when(dishCategoryMapper.toDishCategoryResponse(sampleCategory)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        PagingResult<DishCategoryResponse> result = dishCategoryService.getAllDishCategoryByRestaurantId(1L, "Drink", pageable);

        // Xác nhận kết quả
        assertEquals(1, result.getResults().size(), "Phải tìm thấy 1 category");
        assertEquals(1, result.getTotalItems(), "Tổng số item phải là 1");
        assertEquals("Drinks", result.getResults().get(0).getName(), "Tên category phải là 'Drinks'");
    }

    /**
     * Test getAllDishCategoryByRestaurantId khi không tìm thấy dữ liệu.
     * Kiểm tra nhánh thất bại với phân trang.
     */
    @Test
    public void testGetAllDishCategoryByRestaurantId_NotFound() {
        // Tạo Pageable mẫu
        Pageable pageable = PageRequest.of(0, 10);
        // Mock danh sách category trả về
        List<DishCategory> categories = List.of();
        when(dishCategoryRepository.findByRestaurant_IdAndNameContaining(1L, "Foods", pageable)).thenReturn(categories);
        // Mock đếm số lượng
        when(dishCategoryRepository.countByRestaurant_IdAndNameContaining(1L, "Foods")).thenReturn(0);

        // Gọi phương thức cần test
        PagingResult<DishCategoryResponse> result = dishCategoryService.getAllDishCategoryByRestaurantId(1L, "Foods", pageable);

        // Xác nhận kết quả
        assertEquals(0, result.getResults().size(), "Phải tìm thấy 0 category");
        assertEquals(0, result.getTotalItems(), "Tổng số item phải là 0");
    }

    /**
     * Test findDishCategoryByRestaurantId khi tìm thấy dữ liệu.
     * Kiểm tra nhánh thành công của tìm danh sách.
     */
    @Test
    public void testFindDishCategoryByRestaurantId() {
        // Mock danh sách category trả về
        List<DishCategory> categories = List.of(sampleCategory);
        when(dishCategoryRepository.findByRestaurant_Id(1L)).thenReturn(categories);
        // Mock ánh xạ
        when(dishCategoryMapper.toDishCategoryResponse(sampleCategory)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        List<DishCategoryResponse> result = dishCategoryService.findDishCategoryByRestaurantId(1L);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 category");
        assertEquals("Drinks", result.get(0).getName(), "Tên category phải là 'Drinks'");
    }

    /**
     * Test findDishCategoryByRestaurantId khi không tìm thấy dữ liệu.
     * Kiểm tra nhánh thất bại của tìm danh sách.
     */
    @Test
    public void testFindDishCategoryByRestaurantId_NotFound() {
        // Mock danh sách category trả về
        List<DishCategory> categories = List.of();
        when(dishCategoryRepository.findByRestaurant_Id(1L)).thenReturn(categories);

        // Gọi phương thức cần test
        List<DishCategoryResponse> result = dishCategoryService.findDishCategoryByRestaurantId(1L);

        // Xác nhận kết quả
        assertEquals(0, result.size(), "Phải tìm thấy 0 category");
    }

    /**
     * Test findById khi tìm thấy category.
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testFindById_Found() {
        // Mock tìm thấy category
        when(dishCategoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        // Gọi phương thức cần test
        DishCategory result = dishCategoryService.findById(1L);

        // Xác nhận kết quả
        assertNotNull(result, "Category không được null");
        assertEquals("Drinks", result.getName(), "Tên category phải là 'Drinks'");
    }

    /**
     * Test findById khi không tìm thấy category (ném exception).
     * Kiểm tra nhánh thất bại với AppException.
     */
    @Test
    public void testFindById_NotFound() {
        // Mock không tìm thấy category
        when(dishCategoryRepository.findById(1L)).thenReturn(Optional.empty());
        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            dishCategoryService.findById(1L);
        });

        // Xác nhận lỗi là NOT_EXIST
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode(), "Lỗi phải là NOT_EXIST");
    }

    /**
     * Test findByCodeAndRestaurantId khi tìm thấy category.
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testFindByCodeAndRestaurantId_Found() {
        // Mock tìm thấy category
        when(dishCategoryRepository.findByCodeAndRestaurant_Id("drinks", 1L)).thenReturn(Optional.of(sampleCategory));

        // Gọi phương thức cần test
        DishCategory result = dishCategoryService.findByCodeAndRestaurantId("drinks", 1L);

        // Xác nhận kết quả
        assertNotNull(result, "Category không được null");
        assertEquals("Drinks", result.getName(), "Tên category phải là 'Drinks'");
    }

    /**
     * Test findByCodeAndRestaurantId khi không tìm thấy category (ném exception).
     * Kiểm tra nhánh thất bại với AppException.
     */
    @Test
    public void testFindByCodeAndRestaurantId_NotFound() {
        // Mock không tìm thấy category
        when(dishCategoryRepository.findByCodeAndRestaurant_Id("food", 1L)).thenReturn(Optional.empty());

        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            dishCategoryService.findByCodeAndRestaurantId("food", 1L);
        });

        // Xác nhận lỗi là INVALID_CODE
        assertEquals(ErrorCode.INVALID_CODE, exception.getErrorCode(), "Lỗi phải là INVALID_CODE");
    }

    /**
     * Test deleteCategoryById khi xóa thành công.
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testDeleteCategoryById() {
        // Mock tìm thấy category để xóa
        when(dishCategoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        // Mock hành vi xóa (không ném lỗi)
        doNothing().when(dishCategoryRepository).delete(sampleCategory);

        // Gọi phương thức cần test
        dishCategoryService.deleteCategoryById(1L);

        // Xác nhận phương thức delete được gọi
        verify(dishCategoryRepository).delete(sampleCategory);
    }

    /**
     * Test updateDishCategoryById khi cập nhật thành công.
     * Kiểm tra nhánh thành công của phương thức.
     */
    @Test
    public void testUpdateDishCategoryById() {
        // Tạo DishCategoryRequest mẫu (cho update)
        DishCategoryRequest updateRequest = new DishCategoryRequest();
        updateRequest.setName("Updated Drinks");
        updateRequest.setRestaurantId(1L);

        DishCategory updatedCategory = new DishCategory();
        updatedCategory.setId(sampleCategory.getId());
        updatedCategory.setName("Updated Drinks");
        updatedCategory.setCode(sampleCategory.getCode());


        DishCategoryResponse updatedResponse = DishCategoryResponse.builder().id(1L).name("Updated Drinks").build();

        // Mock tìm thấy category
        when(dishCategoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        // Mock
        doAnswer(invocation -> {
            DishCategory dish = invocation.getArgument(0);
            DishCategoryRequest req = invocation.getArgument(1);
            dish.setName(req.getName()); // mô phỏng hành vi update
            return null;
        }).when(dishCategoryMapper).updateDishCategory(sampleCategory, updateRequest);

        // Mock lưu category sau khi cập nhật
        when(dishCategoryRepository.save(sampleCategory)).thenReturn(updatedCategory);
        // Mock ánh xạ response
        when(dishCategoryMapper.toDishCategoryResponse(updatedCategory)).thenReturn(updatedResponse);

        // Gọi phương thức cần test
        DishCategoryResponse result = dishCategoryService.updateDishCategoryById(updateRequest, 1L);

        // Xác nhận kết quả
        assertNotNull(result, "Response không được null");
        assertEquals("Updated Drinks", result.getName(), "Tên category phải là 'Updated Drinks'");
        // Xác nhận updateDishCategory được gọi để cập nhật entity
        verify(dishCategoryMapper).updateDishCategory(sampleCategory, updateRequest);
    }
}



