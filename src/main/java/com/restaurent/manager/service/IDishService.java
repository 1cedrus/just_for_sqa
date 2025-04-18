package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishRequest;
import com.restaurent.manager.dto.request.dish.DishUpdateRequest;
import com.restaurent.manager.dto.response.DishResponse;
import com.restaurent.manager.entity.Dish;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDishService {
    DishResponse createNewDish(DishRequest request);
    List<DishResponse> findByRestaurantId(Long accountId);
    DishResponse updateDish(Long dishId, DishUpdateRequest request);
    Dish findByDishId(Long dishId);
    List<DishResponse> findDishesByCategoryCode(String categoryCode, Long restaurantId);
    List<DishResponse> findDishesByRestaurantActive(Long restaurantId);
    PagingResult<DishResponse> getDishesByRestaurantIdAndStatus(Long accountId, boolean status, Pageable pageable, String query);
}
