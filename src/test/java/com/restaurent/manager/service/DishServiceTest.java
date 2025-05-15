package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishRequest;
import com.restaurent.manager.dto.request.dish.DishUpdateRequest;
import com.restaurent.manager.dto.response.DishResponse;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Unit;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishMapper;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.service.IDishCategoryService;
import com.restaurent.manager.service.IUnitService;
import com.restaurent.manager.service.IRestaurantService;
import com.restaurent.manager.service.impl.DishService;
import com.restaurent.manager.utils.SlugUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DishServiceTest {

    // Mock các dependency của DishService
    @Mock
    private DishRepository dishRepository;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private IDishCategoryService dishCategoryService;

    @Mock
    private IUnitService unitService;

    @Mock
    private IRestaurantService restaurantService;

    // Inject các mock vào DishService
    @InjectMocks
    private DishService dishService;

    // Biến mẫu để tái sử dụng trong các test case
    private Dish sampleDish;
    private DishRequest sampleRequest;
    private DishUpdateRequest sampleUpdateRequest;
    private DishResponse sampleResponse;
    private DishCategory sampleCategory;
    private Unit sampleUnit;
    private Restaurant sampleRestaurant;

    /**
     * Thiết lập dữ liệu mẫu trước mỗi test case.
     * Khởi tạo các đối tượng mẫu và reset trạng thái mock.
     */
    @BeforeEach
    public void setUp() {
        // Tạo Dish mẫu
        sampleDish = new Dish();
        sampleDish.setId(1L);
        sampleDish.setName("Pizza");
        sampleDish.setCode("pizza");
        sampleDish.setStatus(true);

        // Tạo DishRequest mẫu
        sampleRequest = new DishRequest();
        sampleRequest.setName("Pizza");
        sampleRequest.setDishCategoryId(1L);
        sampleRequest.setUnitId(1L);
        sampleRequest.setRestaurantId(1L);

        // Tạo DishUpdateRequest mẫu
        sampleUpdateRequest = new DishUpdateRequest();
        sampleUpdateRequest.setName("Updated Pizza");
        sampleUpdateRequest.setDishCategoryId(1L);
        sampleUpdateRequest.setUnitId(1L);

        // Tạo DishResponse mẫu
        sampleResponse = new DishResponse();
        sampleResponse.setId(1L);
        sampleResponse.setName("Pizza");

        // Tạo DishCategory mẫu
        sampleCategory = new DishCategory();
        sampleCategory.setId(1L);
        sampleCategory.setCode("food");

        // Tạo Unit mẫu
        sampleUnit = new Unit();
        sampleUnit.setId(1L);

        // Tạo Restaurant mẫu
        sampleRestaurant = new Restaurant();
        sampleRestaurant.setId(1L);
    }

    /**
     * Test createNewDish khi tạo thành công.
     * Không có nhánh điều kiện, kiểm tra logic cơ bản.
     */
    // TestcaseID: DS-1
    @Test
    public void testCreateNewDish_Success() {
        // Mock ánh xạ từ request sang entity
        when(dishMapper.toDish(sampleRequest)).thenReturn(sampleDish);
        // Mock tìm DishCategory, Unit, Restaurant
        when(dishCategoryService.findById(1L)).thenReturn(sampleCategory);
        when(unitService.findById(1L)).thenReturn(sampleUnit);
        when(restaurantService.getRestaurantById(1L)).thenReturn(sampleRestaurant);
        // Mock lưu Dish
        when(dishRepository.save(any(Dish.class))).thenReturn(sampleDish);
        // Mock ánh xạ sang response
        when(dishMapper.toDishResponse(sampleDish)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        DishResponse result = dishService.createNewDish(sampleRequest);

        // Xác nhận kết quả
        assertNotNull(result, "Response không được null");
        assertEquals("Pizza", result.getName(), "Tên dish phải là 'Pizza'");
        // Xác nhận code được tạo từ SlugUtils
        verify(dishRepository).save(argThat(dish -> dish.getCode().equals("pizza")));
    }

    /**
     * Test findByRestaurantId khi tìm thấy dữ liệu.
     * Không có nhánh điều kiện, kiểm tra logic cơ bản.
     */
    // TestcaseID: DS-2
    @Test
    public void testFindByRestaurantId() {
        // Mock danh sách Dish
        List<Dish> dishes = List.of(sampleDish);
        when(dishRepository.findByRestaurant_Id(1L)).thenReturn(dishes);
        // Mock ánh xạ
        when(dishMapper.toDishResponse(sampleDish)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        List<DishResponse> result = dishService.findByRestaurantId(1L);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish");
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    /**
     * Test updateDish khi cập nhật thành công.
     * Không có nhánh điều kiện, kiểm tra logic cơ bản.
     */
    // TestcaseID: DS-3
    @Test
    public void testUpdateDish_Success() {
        // Mock tìm thấy Dish
        when(dishRepository.findById(1L)).thenReturn(Optional.of(sampleDish));
        // Mock tìm DishCategory và Unit
        when(dishCategoryService.findById(1L)).thenReturn(sampleCategory);
        when(unitService.findById(1L)).thenReturn(sampleUnit);
        // Mock lưu Dish
        when(dishRepository.save(sampleDish)).thenReturn(sampleDish);
        // Mock ánh xạ
        when(dishMapper.toDishResponse(sampleDish)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        DishResponse result = dishService.updateDish(1L, sampleUpdateRequest);

        // Xác nhận kết quả
        assertNotNull(result, "Response không được null");
        assertEquals("Pizza", result.getName(), "Tên dish phải là 'Pizza'");
        // Xác nhận updateDish được gọi
        verify(dishMapper).updateDish(sampleDish, sampleUpdateRequest);
    }

    /**
     * Test findByDishId khi tìm thấy Dish.
     * Kiểm tra nhánh thành công của phương thức.
     */
    // TestcaseID: DS-4
    @Test
    public void testFindByDishId_Found() {
        // Mock tìm thấy Dish
        when(dishRepository.findById(1L)).thenReturn(Optional.of(sampleDish));

        // Gọi phương thức cần test
        Dish result = dishService.findByDishId(1L);

        // Xác nhận kết quả
        assertNotNull(result, "Dish không được null");
        assertEquals("Pizza", result.getName(), "Tên dish phải là 'Pizza'");
    }

    /**
     * Test findByDishId khi không tìm thấy Dish (ném exception).
     * Kiểm tra nhánh thất bại của phương thức.
     */
    // TestcaseID: DS-5
    @Test
    public void testFindByDishId_NotFound() {
        // Mock không tìm thấy Dish
        when(dishRepository.findById(1L)).thenReturn(Optional.empty());

        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            dishService.findByDishId(1L);
        });

        // Xác nhận lỗi là NOT_EXIST
        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode(), "Lỗi phải là NOT_EXIST");
    }

    /**
     * Test findDishesByCategoryCode khi categoryCode là "all".
     * Kiểm tra nhánh categoryCode.equals("all") = true.
     */
    // TestcaseID: DS-6
    @Test
    public void testFindDishesByCategoryCode_All() {
        // Mock danh sách Dish
        List<Dish> dishes = List.of(sampleDish);
        when(dishRepository.findByRestaurant_IdAndStatus(1L, true)).thenReturn(dishes);
        // Mock ánh xạ
        when(dishMapper.toDishResponse(sampleDish)).thenReturn(sampleResponse);

        // Gọi phương thức cần test với categoryCode = "all"
        List<DishResponse> result = dishService.findDishesByCategoryCode("all", 1L);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish");
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    /**
     * Test findDishesByCategoryCode khi categoryCode không phải "all".
     * Kiểm tra nhánh categoryCode.equals("all") = false.
     */
    // TestcaseID: DS-7
    @Test
    public void testFindDishesByCategoryCode_SpecificCategory() {
        // Mock tìm DishCategory
        when(dishCategoryService.findByCodeAndRestaurantId("food", 1L)).thenReturn(sampleCategory);
        // Mock danh sách Dish
        List<Dish> dishes = List.of(sampleDish);
        when(dishRepository.findByDishCategory_IdAndStatus(1L, true)).thenReturn(dishes);
        // Mock ánh xạ
        when(dishMapper.toDishResponse(sampleDish)).thenReturn(sampleResponse);

        // Gọi phương thức cần test với categoryCode cụ thể
        List<DishResponse> result = dishService.findDishesByCategoryCode("food", 1L);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish");
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    /**
     * Test findDishesByRestaurantActive khi tìm thấy dữ liệu.
     * Không có nhánh điều kiện, kiểm tra logic cơ bản.
     */
    // TestcaseID: DS-8
    @Test
    public void testFindDishesByRestaurantActive() {
        // Mock danh sách Dish
        List<Dish> dishes = List.of(sampleDish);
        when(dishRepository.findByRestaurant_IdAndStatus(1L, true)).thenReturn(dishes);
        // Mock ánh xạ
        when(dishMapper.toDishResponse(sampleDish)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        List<DishResponse> result = dishService.findDishesByRestaurantActive(1L);

        // Xác nhận kết quả
        assertEquals(1, result.size(), "Phải tìm thấy 1 dish");
        assertEquals("Pizza", result.get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    /**
     * Test getDishesByRestaurantIdAndStatus khi tìm thấy dữ liệu.
     * Không có nhánh điều kiện, kiểm tra logic phân trang.
     */
    // TestcaseID: DS-9
    @Test
    public void testGetDishesByRestaurantIdAndStatus_Found() {
        // Tạo Pageable mẫu
        Pageable pageable = PageRequest.of(0, 10);
        // Mock danh sách Dish
        List<Dish> dishes = List.of(sampleDish);
        when(dishRepository.findByRestaurant_IdAndStatusAndNameContaining(1L, true, pageable, "Piz")).thenReturn(dishes);
        // Mock đếm số lượng
        when(dishRepository.countByRestaurant_IdAndStatusAndNameContaining(1L, true, "Piz")).thenReturn(1);
        // Mock ánh xạ
        when(dishMapper.toDishResponse(sampleDish)).thenReturn(sampleResponse);

        // Gọi phương thức cần test
        PagingResult<DishResponse> result = dishService.getDishesByRestaurantIdAndStatus(1L, true, pageable, "Piz");

        // Xác nhận kết quả
        assertEquals(1, result.getResults().size(), "Phải tìm thấy 1 dish");
        assertEquals(1L, result.getTotalItems(), "Tổng số item phải là 1");
        assertEquals("Pizza", result.getResults().get(0).getName(), "Tên dish phải là 'Pizza'");
    }

    /**
     * Test getDishesByRestaurantIdAndStatus khi không tìm thấy dữ liệu.
     * Không có nhánh điều kiện, kiểm tra trường hợp rỗng.
     */
    // TestcaseID: DS-10
    @Test
    public void testGetDishesByRestaurantIdAndStatus_NotFound() {
        // Tạo Pageable mẫu
        Pageable pageable = PageRequest.of(0, 10);
        // Mock danh sách Dish trống
        when(dishRepository.findByRestaurant_IdAndStatusAndNameContaining(1L, true, pageable, "Piz")).thenReturn(new ArrayList<>());
        // Mock đếm số lượng trả về 0
        when(dishRepository.countByRestaurant_IdAndStatusAndNameContaining(1L, true, "Piz")).thenReturn(0);

        // Gọi phương thức cần test
        PagingResult<DishResponse> result = dishService.getDishesByRestaurantIdAndStatus(1L, true, pageable, "Piz");

        // Xác nhận kết quả
        assertTrue(result.getResults().isEmpty(), "Danh sách results phải trống");
        assertEquals(0L, result.getTotalItems(), "Tổng số item phải là 0");
    }
}