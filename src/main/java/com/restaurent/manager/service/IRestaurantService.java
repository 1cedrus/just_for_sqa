package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.restaurant.*;
import com.restaurent.manager.dto.response.RestaurantResponse;
import com.restaurent.manager.entity.Restaurant;

import java.time.LocalDate;
import java.util.List;

public interface IRestaurantService {
RestaurantResponse initRestaurant(RestaurantRequest request);
List<RestaurantResponse> getRestaurants();

RestaurantResponse updateRestaurant(Long restaurantId,RestaurantUpdateRequest request);
RestaurantResponse updateRestaurant(Long accountId, RestaurantManagerUpdateRequest request);
RestaurantResponse updateRestaurant(Long accountId, RestaurantPaymentRequest request);

Restaurant getRestaurantById(Long id);

RestaurantResponse getRestaurantByAccountId(Long accountId);

double getMoneyToUpdatePackForRestaurant(Long restaurantId,RestaurantUpdateRequest request);
void updateRestaurantVatById(Long restaurantId,boolean status);
RestaurantResponse updatePointForRestaurant(Long restaurantId, PointsRequest request);
int countRestaurantByDateCreated(LocalDate date);
}
