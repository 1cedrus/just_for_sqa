package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishRequest;
import com.restaurent.manager.dto.request.dish.DishUpdateRequest;
import com.restaurent.manager.dto.response.DishResponse;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishMapper;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.service.IDishCategoryService;
import com.restaurent.manager.service.IDishService;
import com.restaurent.manager.service.IRestaurantService;
import com.restaurent.manager.service.IUnitService;
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
public class DishService implements IDishService {
    // Repository for accessing Dish data
    DishRepository dishRepository;

    // Mapper for converting between entities and DTOs
    DishMapper dishMapper;

    // Service for handling DishCategory-related operations
    IDishCategoryService dishCategoryService;

    // Service for handling Unit-related operations
    IUnitService unitService;

    // Service for handling Restaurant-related operations
    IRestaurantService restaurantService;

    @Override
    public DishResponse createNewDish(DishRequest request) {
        // Map the request to a Dish entity
        Dish dish = dishMapper.toDish(request);

        // Generate a slug code for the Dish name
        dish.setCode(SlugUtils.toSlug(dish.getName()));

        // Set the associated DishCategory, Unit, and Restaurant entities
        dish.setDishCategory(dishCategoryService.findById(request.getDishCategoryId()));
        dish.setUnit(unitService.findById(request.getUnitId()));
        dish.setRestaurant(restaurantService.getRestaurantById(request.getRestaurantId()));

        // Set the initial status of the Dish to active
        dish.setStatus(true);

        // Save the Dish entity and return the response
        return dishMapper.toDishResponse(dishRepository.save(dish));
    }

    @Override
    public List<DishResponse> findByRestaurantId(Long restaurantId) {
        // Fetch all Dishes by restaurant ID and map them to responses
        return dishRepository.findByRestaurant_Id(restaurantId)
                .stream()
                .map(dishMapper::toDishResponse)
                .toList();
    }

    @Override
    public DishResponse updateDish(Long dishId, DishUpdateRequest request) {
        // Find the Dish by ID
        Dish dish = findByDishId(dishId);

        // Update the Dish entity with the new data from the request
        dishMapper.updateDish(dish, request);

        // Update the associated DishCategory and Unit entities
        dish.setDishCategory(dishCategoryService.findById(request.getDishCategoryId()));
        dish.setUnit(unitService.findById(request.getUnitId()));

        // Regenerate the slug code for the updated Dish name
        dish.setCode(SlugUtils.toSlug(dish.getName()));

        // Save the updated Dish entity and return the response
        dishRepository.save(dish);
        return dishMapper.toDishResponse(dish);
    }

    @Override
    public Dish findByDishId(Long dishId) {
        // Find a Dish by ID or throw an exception if not found
        return dishRepository.findById(dishId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public List<DishResponse> findDishesByCategoryCode(String categoryCode, Long restaurantId) {
        // If the category code is "all", fetch all active Dishes for the restaurant
        if (categoryCode.equals("all")) {
            return dishRepository.findByRestaurant_IdAndStatus(restaurantId, true)
                    .stream()
                    .map(dishMapper::toDishResponse)
                    .toList();
        }

        // Find the DishCategory by code and restaurant ID
        DishCategory category = dishCategoryService.findByCodeAndRestaurantId(categoryCode, restaurantId);

        // Fetch all active Dishes for the category and map them to responses
        return dishRepository.findByDishCategory_IdAndStatus(category.getId(), true)
                .stream()
                .map(dishMapper::toDishResponse)
                .toList();
    }

    @Override
    public List<DishResponse> findDishesByRestaurantActive(Long restaurantId) {
        // Fetch all active Dishes for the restaurant and map them to responses
        return dishRepository.findByRestaurant_IdAndStatus(restaurantId, true)
                .stream()
                .map(dishMapper::toDishResponse)
                .toList();
    }

    @Override
    public PagingResult<DishResponse> getDishesByRestaurantIdAndStatus(Long restaurantId, boolean status, Pageable pageable, String query) {
        // Fetch paginated Dishes by restaurant ID, status, and name query
        return PagingResult.<DishResponse>builder()
                .results(dishRepository.findByRestaurant_IdAndStatusAndNameContaining(restaurantId, status, pageable, query)
                        .stream()
                        .map(dishMapper::toDishResponse)
                        .toList())
                .totalItems(dishRepository.countByRestaurant_IdAndStatusAndNameContaining(restaurantId, status, query))
                .build();
    }
}