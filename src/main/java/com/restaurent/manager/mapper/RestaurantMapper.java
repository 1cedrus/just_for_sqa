package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.restaurant.RestaurantManagerUpdateRequest;
import com.restaurent.manager.dto.request.restaurant.RestaurantPaymentRequest;
import com.restaurent.manager.dto.request.restaurant.RestaurantRequest;
import com.restaurent.manager.dto.request.restaurant.RestaurantUpdateRequest;
import com.restaurent.manager.dto.response.RestaurantResponse;
import com.restaurent.manager.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    Restaurant toRestaurant(RestaurantRequest request);
    RestaurantResponse toRestaurantResponse(Restaurant restaurant);

    void updateRestaurant(@MappingTarget Restaurant restaurant, RestaurantUpdateRequest request);
    void updateRestaurant(@MappingTarget Restaurant restaurant, RestaurantManagerUpdateRequest request);
    void updateRestaurant(@MappingTarget Restaurant restaurant, RestaurantPaymentRequest request);

}
