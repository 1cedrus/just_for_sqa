package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishCategoryRequest;
import com.restaurent.manager.dto.response.DishCategoryResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.DishCategory;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishCategoryMapper;
import com.restaurent.manager.repository.DishCategoryRepository;
import com.restaurent.manager.service.IAccountService;
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
    DishCategoryRepository dishCategoryRepository;
    DishCategoryMapper dishCategoryMapper;
    IAccountService accountService;
    IRestaurantService restaurantService;
    @Override
    public DishCategoryResponse createDishCategory(DishCategoryRequest request) {
        if(dishCategoryRepository.existsByNameAndRestaurant_Id(request.getName(), request.getRestaurantId())){
            throw new AppException(ErrorCode.DISH_CATEGORY_EXIST);
        }
        DishCategory dishCategory = dishCategoryMapper.toDishCategory(request);
        dishCategory.setCode(SlugUtils.toSlug(dishCategory.getName()));
        dishCategory.setRestaurant(restaurantService.getRestaurantById(request.getRestaurantId()));
        return dishCategoryMapper.toDishCategoryResponse(
                dishCategoryRepository.save(
                        dishCategory));
    }

    @Override
    public PagingResult<DishCategoryResponse> getAllDishCategoryByRestaurantId(Long restaurantId, String query, Pageable pageable) {
        return PagingResult.<DishCategoryResponse>builder()
                .results(dishCategoryRepository.findByRestaurant_IdAndNameContaining(restaurantId,query,pageable).stream().map(dishCategoryMapper::toDishCategoryResponse)
                        .toList())
                .totalItems(dishCategoryRepository.countByRestaurant_IdAndNameContaining(restaurantId,query))
                .build();
    }

    @Override
    public List<DishCategoryResponse> findDishCategoryByRestaurantId(Long restaurantId) {
        return dishCategoryRepository.findByRestaurant_Id(restaurantId).stream().map(dishCategoryMapper::toDishCategoryResponse).toList();
    }

    @Override
    public DishCategory findById(Long id) {
        return dishCategoryRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public DishCategory findByCodeAndRestaurantId(String code, Long restaurantId) {
        return dishCategoryRepository.findByCodeAndRestaurant_Id(code,restaurantId).orElseThrow(
                () -> new AppException(ErrorCode.INVALID_CODE)
        );
    }

    @Override
    public void deleteCategoryById(Long id) {
        DishCategory category = findById(id);
        dishCategoryRepository.delete(category);
    }

    @Override
    public DishCategoryResponse updateDishCategoryById(DishCategoryRequest request, Long id) {
        DishCategory dishCategory = findById(id);
        dishCategoryMapper.updateDishCategory(dishCategory,request);
        dishCategoryRepository.save(dishCategory);
        return dishCategoryMapper.toDishCategoryResponse(dishCategory);
    }
}
