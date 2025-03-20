package com.restaurent.manager.controller;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import com.restaurent.manager.service.IDishOrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@RequestMapping(value = "/api/dish-order")
@SecurityRequirement(name = "bearerAuth")
public class DishOrderController {
    IDishOrderService dishOrderService;
    SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/change-status")
    public void updateStatusDishOrderById(@RequestBody DishOrderResponse request){
        DishOrderResponse response = dishOrderService.changeStatusDishOrderById(request.getId(), DISH_ORDER_STATE.valueOf(request.getStatus()));
        String roomId = "" + request.getOrder().getTableRestaurant().getId();
        messagingTemplate.convertAndSend("/topic/table/" + roomId, response);
    }
    @PreAuthorize(value = "hasAnyRole('WAITER', 'CHEF')")
    @GetMapping(value = "/{orderId}")
    public ApiResponse<PagingResult<DishOrderResponse>> findDishOrderByOrderId(@PathVariable Long orderId, @RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<PagingResult<DishOrderResponse>>builder()
                .result(dishOrderService.findDishOrderByOrderId(orderId,pageable))
                .build();
    }
    @GetMapping(value = "/{orderId}/state/{state}")
    public ApiResponse<List<DishOrderResponse>> findDishOrderByOrderIdAndState(@PathVariable Long orderId,@PathVariable DISH_ORDER_STATE state){
        return ApiResponse.<List<DishOrderResponse>>builder()
                .result(dishOrderService.findDishOrderByOrderIdAndStatus(orderId,state))
                .build();
    }
    @PreAuthorize(value = "hasRole('CHEF')")
    @GetMapping(value = "/restaurant/{restaurantId}/state/{state}")
    public ApiResponse<List<DishOrderResponse>> findDishOrderByRestaurantIdAndState(@PathVariable Long restaurantId,@PathVariable DISH_ORDER_STATE state){
        return ApiResponse.<List<DishOrderResponse>>builder()
                .result(dishOrderService.findDishOrderByRestaurantIdAndState(restaurantId,state))
                .build();
    }
    @PreAuthorize(value = "hasRole('CHEF')")
    @GetMapping(value = "/restaurant/{restaurantId}")
    public ApiResponse<List<DishOrderResponse>> findDishOrderWaitingByRestaurantId(@PathVariable Long restaurantId){
        return ApiResponse.<List<DishOrderResponse>>builder()
                .result(dishOrderService.findDishOrderWaitingByAndRestaurantId(restaurantId))
                .build();
    }
}
