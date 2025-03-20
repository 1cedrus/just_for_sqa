package com.restaurent.manager.controller;

import com.restaurent.manager.dto.request.restaurant.*;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.RestaurantResponse;
import com.restaurent.manager.service.IRestaurantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {
    IRestaurantService restaurantService;
    @PostMapping("/init")
    public ApiResponse<RestaurantResponse> initRestaurant(@RequestBody @Valid RestaurantRequest request){
        return ApiResponse.<RestaurantResponse>builder()
                .result(restaurantService.initRestaurant(request))
                .build();
    }
    @PreAuthorize(value = "hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<RestaurantResponse>> getRestaurants(){
        return ApiResponse.<List<RestaurantResponse>>builder()
                .result(restaurantService.getRestaurants())
                .build();
    }
    @PreAuthorize(value = "hasRole('ADMIN')")
    @PutMapping("/admin/{restaurantId}")
    public ApiResponse<RestaurantResponse> updateRestaurant(@PathVariable Long restaurantId ,@RequestBody @Valid RestaurantUpdateRequest request){
        return ApiResponse.<RestaurantResponse>builder()
                .result(restaurantService.updateRestaurant(restaurantId,request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PutMapping("/manager/{accountId}")
    public ApiResponse<RestaurantResponse> updateRestaurantByManager(@PathVariable Long accountId,@RequestBody @Valid RestaurantManagerUpdateRequest request){
        return ApiResponse.<RestaurantResponse>builder()
                .result(restaurantService.updateRestaurant(accountId,request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PutMapping("/manager/payment/{accountId}")
    public ApiResponse<RestaurantResponse> updateRestaurantPayment(@PathVariable Long accountId,@RequestBody @Valid RestaurantPaymentRequest request){
        return ApiResponse.<RestaurantResponse>builder()
                .result(restaurantService.updateRestaurant(accountId,request))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('ADMIN','WAITER','MANAGER')")
    @GetMapping("/account/{accountID}")
    public ApiResponse<RestaurantResponse> getRestaurantByAccountId(@PathVariable Long accountID){
        return ApiResponse.<RestaurantResponse>builder()
                .result(restaurantService.getRestaurantByAccountId(accountID))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PostMapping(value = "/{restaurantId}/pack/require-money")
    public ApiResponse<Double> getRequireMoneyToUpdatePackForRestaurant(@PathVariable Long restaurantId,@RequestBody @Valid RestaurantUpdateRequest request){
        return ApiResponse.<Double>builder()
                .result(restaurantService.getMoneyToUpdatePackForRestaurant(restaurantId,request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PutMapping(value = "/{restaurant}/vat/{status}")
    public ApiResponse<Void> updateVatStatusForRestaurantById(@PathVariable Long restaurant,@PathVariable boolean status){
        restaurantService.updateRestaurantVatById(restaurant,status);
        return ApiResponse.<Void>builder()
                .message("Update success")
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PutMapping(value = "/{restaurantId}/point")
    public ApiResponse<RestaurantResponse> updatePointForRestaurant(@PathVariable Long restaurantId, @RequestBody @Valid PointsRequest request){
        return ApiResponse.<RestaurantResponse>builder()
                .result(restaurantService.updatePointForRestaurant(restaurantId,request))
                .build();
    }
}
