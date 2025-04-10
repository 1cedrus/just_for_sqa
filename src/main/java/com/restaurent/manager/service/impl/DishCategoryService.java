package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishCategoryRequest;
import com.restaurent.manager.dto.response.DishCategoryResponse;
import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishCategoryMapper;
import com.restaurent.manager.repository.DishCategoryRepository;
import com.restaurent.manager.service.IDishCategoryService;
import com.restaurent.manager.service.IRestaurantService;
import com.restaurent.manager.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
public class DishCategoryService implements IDishCategoryService {
    // Repository for accessing DishCategory data
    DishCategoryRepository dishCategoryRepository;

    // Mapper for converting between entities and DTOs
    DishCategoryMapper dishCategoryMapper;

    // Service for handling Restaurant-related operations
    IRestaurantService restaurantService;

    @Override
    public DishCategoryResponse createDishCategory(DishCategoryRequest request) {
        // Check if a DishCategory with the same name and restaurant ID already exists
        if (dishCategoryRepository.existsByNameAndRestaurant_Id(request.getName(), request.getRestaurantId())) {
            throw new AppException(ErrorCode.DISH_CATEGORY_EXIST);
        }

        // Map the request to a DishCategory entity
        DishCategory dishCategory = dishCategoryMapper.toDishCategory(request);

        // Generate a slug code for the DishCategory name
        dishCategory.setCode(SlugUtils.toSlug(dishCategory.getName()));

        // Set the associated Restaurant entity
        dishCategory.setRestaurant(restaurantService.getRestaurantById(request.getRestaurantId()));

        // Save the DishCategory and return the response
        return dishCategoryMapper.toDishCategoryResponse(
            dishCategoryRepository.save(dishCategory));
    }

    @Override
    public PagingResult<DishCategoryResponse> getAllDishCategoryByRestaurantId(Long restaurantId, String query, Pageable pageable) {
        // Fetch paginated DishCategories by restaurant ID and name query
        return PagingResult.<DishCategoryResponse>builder()
            .results(dishCategoryRepository.findByRestaurant_IdAndNameContaining(restaurantId, query, pageable)
                .stream()
                .map(dishCategoryMapper::toDishCategoryResponse)
                .toList())
            .totalItems(dishCategoryRepository.countByRestaurant_IdAndNameContaining(restaurantId, query))
            .build();
    }

    @Override
    public List<DishCategoryResponse> findDishCategoryByRestaurantId(Long restaurantId) {
        // Fetch all DishCategories by restaurant ID
        return dishCategoryRepository.findByRestaurant_Id(restaurantId)
            .stream()
            .map(dishCategoryMapper::toDishCategoryResponse)
            .toList();
    }

    @Override
    public DishCategory findById(Long id) {
        // Find a DishCategory by ID or throw an exception if not found
        return dishCategoryRepository.findById(id).orElseThrow(
            () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public DishCategory findByCodeAndRestaurantId(String code, Long restaurantId) {
        // Find a DishCategory by code and restaurant ID or throw an exception if not found
        return dishCategoryRepository.findByCodeAndRestaurant_Id(code, restaurantId).orElseThrow(
            () -> new AppException(ErrorCode.INVALID_CODE)
        );
    }

    @Override
    public void deleteCategoryById(Long id) {
        // Find the DishCategory by ID and delete it
        DishCategory category = findById(id);
        dishCategoryRepository.delete(category);
    }

    @Override
    public DishCategoryResponse updateDishCategoryById(DishCategoryRequest request, Long id) {
        // Find the DishCategory by ID
        DishCategory dishCategory = findById(id);

        // Update the DishCategory with the new data from the request
        dishCategoryMapper.updateDishCategory(dishCategory, request);

        // Save the updated DishCategory and return the response
        dishCategoryRepository.save(dishCategory);
        return dishCategoryMapper.toDishCategoryResponse(dishCategory);
    }
}