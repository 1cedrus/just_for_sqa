package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.entity.DishOrder;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishOrderMapper;
import com.restaurent.manager.repository.DishOrderRepository;
import com.restaurent.manager.service.IDishOrderService;
import com.restaurent.manager.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class DishOrderService implements IDishOrderService {
    // Repository for accessing DishOrder data
    DishOrderRepository dishOrderRepository;

    // Mapper for converting between entities and DTOs
    DishOrderMapper dishOrderMapper;

    // Service for handling Order-related operations
    IOrderService orderService;

    @Override
    public DishOrderResponse changeStatusDishOrderById(Long id, DISH_ORDER_STATE status) {
        // Find the DishOrder by ID
        DishOrder dishOrder = findById(id);

        // Update the status of the DishOrder
        dishOrder.setStatus(status);

        // Save the updated DishOrder and return the response
        dishOrderRepository.save(dishOrder);
        return dishOrderMapper.toDishOrderResponse(dishOrder);
    }

    @Override
    public List<DishOrderResponse> findDishOrderWaitingByAndRestaurantId(Long restaurantId) {
        // Initialize a list to store DishOrder responses
        List<DishOrderResponse> dishOrderResponses = new ArrayList<>();

        // Find DishOrders with WAITING state
        List<DishOrderResponse> waitingOrder = findDishOrderByRestaurantIdAndState(restaurantId, DISH_ORDER_STATE.WAITING);

        // Find DishOrders with PREPARE state
        List<DishOrderResponse> prepareOrder = findDishOrderByRestaurantIdAndState(restaurantId, DISH_ORDER_STATE.PREPARE);

        // Add WAITING orders to the response list
        if (!waitingOrder.isEmpty()) {
            dishOrderResponses.addAll(waitingOrder);
        }

        // Add PREPARE orders to the response list
        if (!prepareOrder.isEmpty()) {
            dishOrderResponses.addAll(prepareOrder);
        }

        return dishOrderResponses;
    }

    @Override
    public DishOrder findById(Long dishOrderId) {
        // Find a DishOrder by ID or throw an exception if not found
        return dishOrderRepository.findById(dishOrderId).orElseThrow(
            () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public PagingResult<DishOrderResponse> findDishOrderByOrderId(Long orderId, Pageable pageable) {
        // Fetch paginated DishOrders by order ID
        return PagingResult.<DishOrderResponse>builder()
            .results(dishOrderRepository.findDishOrderByOrder_Id(orderId, pageable)
                .stream()
                .map(dishOrderMapper::toDishOrderResponse)
                .toList())
            .totalItems(dishOrderRepository.countByOrder_Id(orderId))
            .build();
    }

    @Override
    public List<DishOrderResponse> findDishOrderByOrderIdAndStatus(Long orderId, DISH_ORDER_STATE state) {
        // Get the current date and calculate the start and end of the day
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // Fetch DishOrders by order ID, status, and date range
        return dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(orderId, state, startOfDay, endOfDay)
            .stream()
            .map(dishOrderMapper::toDishOrderResponse)
            .toList();
    }

    @Override
    public List<DishOrderResponse> findDishOrderByRestaurantIdAndState(Long restaurantId, DISH_ORDER_STATE state) {
        // Fetch all orders for the given restaurant ID
        List<Order> orders = orderService.findOrderByRestaurantId(restaurantId);

        // Initialize a list to store DishOrders
        List<DishOrder> dishOrders = new ArrayList<>();

        // Get the current date and calculate the start and end of the day
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // Iterate through the orders and fetch DishOrders for each order
        if (!orders.isEmpty()) {
            for (Order order : orders) {
                List<DishOrder> orderDishes = dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(order.getId(), state, startOfDay, endOfDay);
                if (!orderDishes.isEmpty()) {
                    dishOrders.addAll(orderDishes);
                }
            }
        }

        // Map the DishOrders to responses and return
        return dishOrders.stream().map(dishOrderMapper::toDishOrderResponse).toList();
    }
}