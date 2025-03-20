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
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.DishCategoryRepository;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.repository.UnitRepository;
import com.restaurent.manager.service.*;
import com.restaurent.manager.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
public class DishService implements IDishService {
    DishRepository dishRepository;
    DishMapper dishMapper;
    IDishCategoryService dishCategoryService;
    IUnitService unitService;
    IAccountService accountService;
    IRestaurantService restaurantService;
    @Override
    public DishResponse createNewDish(DishRequest request) {
        Dish dish = dishMapper.toDish(request);
        dish.setCode(SlugUtils.toSlug(dish.getName()));
        dish.setDishCategory(dishCategoryService.findById(request.getDishCategoryId()));
        dish.setUnit(unitService.findById(request.getUnitId()));
        dish.setRestaurant(restaurantService.getRestaurantById(request.getRestaurantId()));
        dish.setStatus(true);
        return dishMapper.toDishResponse(dishRepository.save(dish));
    }

    @Override
    public List<DishResponse> findByRestaurant_Id(Long restaurantId) {
        return dishRepository.findByRestaurant_Id(restaurantId).stream().map(dishMapper::toDishResponse).toList();
    }

    @Override
    public DishResponse updateDish(Long dishId, DishUpdateRequest request) {
        Dish dish = findByDishId(dishId);
        dishMapper.updateDish(dish,request);
        dish.setDishCategory(dishCategoryService.findById(request.getDishCategoryId()));
        dish.setUnit(unitService.findById(request.getUnitId()));
        dish.setCode(SlugUtils.toSlug(dish.getName()));
        dishRepository.save(dish);
        return dishMapper.toDishResponse(dish);
    }

    @Override
    public Dish findByDishId(Long dishId) {
        return dishRepository.findById(dishId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public List<DishResponse> findDishesByCategoryCode(String categoryCode, Long restaurantId) {
        if(categoryCode.equals("all")){
            return dishRepository.findByRestaurant_IdAndStatus(restaurantId,true).stream().map(dishMapper::toDishResponse).toList();
        }
        DishCategory category = dishCategoryService.findByCodeAndRestaurantId(categoryCode,restaurantId);
        return dishRepository.findByDishCategory_IdAndStatus(category.getId(),true).stream().map(dishMapper::toDishResponse).toList();
    }

    @Override
    public List<DishResponse> findDishesByRestaurantActive(Long restaurantId) {
        return dishRepository.findByRestaurant_IdAndStatus(restaurantId,true).stream().map(dishMapper::toDishResponse).toList();
    }

    @Override
    public PagingResult<DishResponse> getDishesByRestaurantIdAndStatus(Long restaurantId, boolean status, Pageable pageable, String query) {
        return PagingResult.<DishResponse>builder()
                .results(dishRepository.findByRestaurant_IdAndStatusAndNameContaining(restaurantId,status,pageable, query).stream().map(dishMapper::toDishResponse).toList())
                .totalItems(dishRepository.countByRestaurant_IdAndStatusAndNameContaining(restaurantId,status,query))
                .build();
    }
}
