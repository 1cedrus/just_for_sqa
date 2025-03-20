package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishCategoryRequest;
import com.restaurent.manager.dto.response.DishCategoryResponse;
import com.restaurent.manager.entity.DishCategory;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDishCategoryService {
    DishCategoryResponse createDishCategory(DishCategoryRequest request);
    PagingResult<DishCategoryResponse> getAllDishCategoryByRestaurantId(Long restaurantId, String query, Pageable pageable);
    List<DishCategoryResponse> findDishCategoryByRestaurantId(Long restaurantId);
    DishCategory findById(Long id);
    DishCategory findByCodeAndRestaurantId(String code, Long restaurantId);
    void deleteCategoryById(Long id);
    DishCategoryResponse updateDishCategoryById(DishCategoryRequest request, Long id);
}
