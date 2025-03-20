package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.dish.DishCategoryRequest;
import com.restaurent.manager.dto.response.DishCategoryResponse;
import com.restaurent.manager.entity.DishCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DishCategoryMapper {

    DishCategory toDishCategory(DishCategoryRequest req);
    DishCategoryResponse toDishCategoryResponse(DishCategory dishCategory);

    void updateDishCategory(@MappingTarget DishCategory dishCategory, DishCategoryRequest request);
}
