package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.dish.DishRequest;
import com.restaurent.manager.dto.request.dish.DishUpdateRequest;
import com.restaurent.manager.dto.response.DishResponse;
import com.restaurent.manager.entity.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DishMapper {
    Dish toDish(DishRequest req);
    DishResponse toDishResponse(Dish dish);
    void updateDish(@MappingTarget Dish dish, DishUpdateRequest request);
}
