package com.restaurent.manager.controller;

import com.restaurent.manager.dto.request.order.DishOrderAddRequest;
import com.restaurent.manager.dto.request.order.OrderRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.dto.response.order.OrderResponse;
import com.restaurent.manager.service.IOrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    IOrderService orderService;
    SimpMessagingTemplate messagingTemplate;
    @PreAuthorize(value = "hasRole('WAITER')")
    @PostMapping(value = "/create")
    public ApiResponse<OrderResponse> createNewOrder(@RequestBody @Valid OrderRequest request){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(request))
                .build();
    }
    @MessageMapping("/restaurant/{restaurantId}/addDishes")
    public void addDishToOrder(@DestinationVariable Long restaurantId, @Payload DishOrderAddRequest request){
        log.info(request.toString());
        String roomId = "" + restaurantId;
        List<DishOrderResponse> responses = orderService.addDishToOrder(request.getOrderId(),request.getDishOrderRequests());
        messagingTemplate.convertAndSend("/topic/order/restaurant/" + roomId,responses);
    }
    @PreAuthorize(value = "hasRole('WAITER')")
    @PutMapping("/order/add-dishes")
    public ApiResponse<List<DishOrderResponse>> addDishes(@RequestBody @Valid DishOrderAddRequest request){
        log.info(request.toString());
        orderService.addDishToOrder(request.getOrderId(),request.getDishOrderRequests());
        return ApiResponse.<List<DishOrderResponse>>builder()
                .message("Order success")
                .build();
    }
    @GetMapping(value = "/{orderId}")
    public ApiResponse<List<DishOrderResponse>> findDishByOrderId(@PathVariable Long orderId){
        return ApiResponse.<List<DishOrderResponse>>builder()
                .result(orderService.findDishByOrderId(orderId))
                .build();
    }
    @MessageMapping("/message")
    @SendTo("/topic/order")
    public String sendMessage(@Payload String message){
        return message;
    }
    @PreAuthorize(value = "hasAnyRole('WAITER', 'CHEF')")
    @GetMapping(value = "/api/order/table/{tableId}")
    public ApiResponse<OrderResponse> findOrderByTableId(@PathVariable Long tableId){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.findOrderByTableId(tableId))
                .build();
    }
    @PreAuthorize(value = "hasRole('WAITER')")
    @GetMapping(value = "/api/order/{orderId}")
    public ApiResponse<OrderResponse> findOrderById(@PathVariable Long orderId){
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.findOrderAndConvertDTOByOrderId(orderId))
                .build();
    }
}
