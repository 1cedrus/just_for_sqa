package com.restaurent.manager.controller;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.dish.DishRequest;
import com.restaurent.manager.dto.request.dish.DishUpdateRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.DishResponse;
import com.restaurent.manager.service.IDishService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/dish")
@FieldDefaults(makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DishController {
    IDishService dishService;
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('DISH')")
    @PostMapping(value = "/create")
    public ApiResponse<DishResponse> createDish(@RequestBody @Valid DishRequest request){
        return ApiResponse.<DishResponse>builder()
                .result(dishService.createNewDish(request))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER','HOSTESS') and hasAuthority('DISH')")
    @GetMapping(value = "/restaurant/{restaurantId}")
    public ApiResponse<List<DishResponse>> findDishesByAccountId(@PathVariable Long restaurantId){
        return ApiResponse.<List<DishResponse>>builder()
                .result(dishService.findByRestaurant_Id(restaurantId))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER','HOSTESS') and hasAuthority('DISH')")
    @GetMapping(value = "/restaurant/{restaurantId}/active")
    public ApiResponse<List<DishResponse>> findDishesByRestaurant(@PathVariable Long restaurantId){
        return ApiResponse.<List<DishResponse>>builder()
                .result(dishService.findDishesByRestaurantActive(restaurantId))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER', 'HOSTESS') and hasAuthority('DISH')")
    @GetMapping(value = "/category/{categoryCode}/restaurant/{restaurantId}")
    public ApiResponse<List<DishResponse>> findDishesByCategoryCode(@PathVariable String categoryCode, @PathVariable Long restaurantId){
        return ApiResponse.<List<DishResponse>>builder()
                .result(dishService.findDishesByCategoryCode(categoryCode,restaurantId))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('DISH')")
    @PutMapping(value = "/{dishId}")
    public ApiResponse<DishResponse> updateDishById(@PathVariable Long dishId, @RequestBody @Valid DishUpdateRequest request){
        return ApiResponse.<DishResponse>builder()
                .result(dishService.updateDish(dishId,request))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER','HOSTESS') and hasAuthority('DISH')")
    @GetMapping(value = "/restaurant/{restaurantId}/{status}")
    public ApiResponse<PagingResult<DishResponse>> findDishesByAccountIdAndStatus(@PathVariable Long restaurantId, @PathVariable boolean status, @RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size
    , @RequestParam(value = "query", defaultValue = "") String query){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<PagingResult<DishResponse>>builder()
                .result(dishService.getDishesByRestaurantIdAndStatus(restaurantId,status,pageable,query))
                .build();
    }
}
