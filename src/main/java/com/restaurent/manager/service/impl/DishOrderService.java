package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.DishOrder;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.entity.TableRestaurant;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishOrderMapper;
import com.restaurent.manager.repository.DishOrderRepository;
import com.restaurent.manager.service.IDishOrderService;
import com.restaurent.manager.service.IOrderService;
import com.restaurent.manager.service.ITableRestaurantService;
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
    DishOrderRepository dishOrderRepository;
    DishOrderMapper dishOrderMapper;
    IOrderService orderService;
    ITableRestaurantService tableRestaurantService;
    @Override
    public DishOrderResponse changeStatusDishOrderById(Long id, DISH_ORDER_STATE status) {
        DishOrder dishOrder = findById(id);
        dishOrder.setStatus(status);
        dishOrderRepository.save(dishOrder);
        return dishOrderMapper.toDishOrderResponse(dishOrder);
    }

    @Override
    public List<DishOrderResponse> findDishOrderWaitingByAndRestaurantId(Long restaurantId) {
        List<DishOrderResponse> dishOrderResponses = new ArrayList<>();
        List<DishOrderResponse> waitingOrder = findDishOrderByRestaurantIdAndState(restaurantId,DISH_ORDER_STATE.WAITING);
        List<DishOrderResponse> prepareOrder = findDishOrderByRestaurantIdAndState(restaurantId,DISH_ORDER_STATE.PREPARE);
        if(waitingOrder != null){
            dishOrderResponses.addAll(waitingOrder);
        }
        if(prepareOrder != null){
            dishOrderResponses.addAll(prepareOrder);
        }
        return dishOrderResponses;
    }

    @Override
    public DishOrder findById(Long dishOrderId) {
        return dishOrderRepository.findById(dishOrderId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public PagingResult<DishOrderResponse> findDishOrderByOrderId(Long orderId, Pageable pageable) {
        return PagingResult.<DishOrderResponse>builder()
                .results(dishOrderRepository.findDishOrderByOrder_Id(orderId,pageable).stream().map(dishOrderMapper::toDishOrderResponse).toList())
                .totalItems(dishOrderRepository.countByOrder_Id(orderId))
                .build();
    }

    @Override
    public List<DishOrderResponse> findDishOrderByOrderIdAndStatus(Long orderId, DISH_ORDER_STATE state) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(orderId,state,startOfDay,endOfDay).stream().map(dishOrderMapper::toDishOrderResponse).toList();
    }

    @Override
    public List<DishOrderResponse> findDishOrderByRestaurantIdAndState(Long restaurantId, DISH_ORDER_STATE state) {
        List<Order> orders = orderService.findOrderByRestaurantId(restaurantId);
        List<DishOrder> dishOrders = new ArrayList<>();
        List<DishOrder> orderDishes;
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        if(!orders.isEmpty()){
            for (Order order: orders){
                orderDishes = dishOrderRepository.findDishOrderByOrder_IdAndStatusAndOrderDateBetweenOrderByOrderDate(order.getId(),state,startOfDay,endOfDay);
                if(!orderDishes.isEmpty()){
                    dishOrders.addAll(orderDishes);
                }
            }
        }
        return dishOrders.stream().map(dishOrderMapper::toDishOrderResponse).toList();
    }
}
