package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.order.DishOrderRequest;
import com.restaurent.manager.dto.request.order.OrderRequest;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.dto.response.order.OrderResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.enums.DISH_ORDER_STATE;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.DishOrderMapper;
import com.restaurent.manager.mapper.OrderMapper;
import com.restaurent.manager.repository.DishOrderRepository;
import com.restaurent.manager.repository.OrderRepository;
import com.restaurent.manager.repository.TableRestaurantRepository;
import com.restaurent.manager.service.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    OrderRepository orderRepository;
    IEmployeeService employeeService;
    ITableRestaurantService tableRestaurantService;
    IRestaurantService restaurantService;
    TableRestaurantRepository tableRestaurantRepository;
    OrderMapper orderMapper;
    IDishService dishService;
    DishOrderMapper dishOrderMapper;
    DishOrderRepository dishOrderRepository;
    IComboService comboService;
    ICustomerService customerService;
    @Override
    public OrderResponse createOrder(OrderRequest request) {
        TableRestaurant tableRestaurant = tableRestaurantService.findById(request.getTableId());
        Order order = new Order();
        Customer customer = customerService.findCustomerByPhoneNumber(request.getCustomerResponse().getPhoneNumber(),request.getRestaurantId());
        order.setCustomer(customer);
        order.setRestaurant(restaurantService.getRestaurantById(request.getRestaurantId()));
        order.setTableRestaurant(tableRestaurant);
        order.setEmployee(employeeService.findEmployeeById(request.getEmployeeId()));
        order.setOrderDate(LocalDate.now());
        Order orderSaved = orderRepository.save(order);
        tableRestaurant.setOrderCurrent(orderSaved.getId());
        tableRestaurantRepository.save(tableRestaurant);
        return orderMapper.toOrderResponse(orderSaved);
    }

    @Override
    public List<DishOrderResponse> addDishToOrder(Long orderId, List<DishOrderRequest> requestList) {
        Order order = findOrderById(orderId);
        Set<DishOrder> dishOrders = order.getDishOrders();
        List<DishOrderResponse> results = new ArrayList<>();
        for (DishOrderRequest request : requestList){
                DishOrder dishOrder = dishOrderMapper.toDishOrder(request);
                if(request.getDishId() != null){
                    dishOrder.setDish(dishService.findByDishId(request.getDishId()));
                }else{
                    dishOrder.setCombo(comboService.findComboById(request.getComboId()));
                }
                dishOrder.setOrder(order);
                dishOrder.setStatus(DISH_ORDER_STATE.WAITING);
                dishOrder.setOrderDate(LocalDateTime.now());
                DishOrder saved = dishOrderRepository.save(dishOrder);
                results.add(dishOrderMapper.toDishOrderResponse(saved));
                if(dishOrders != null){
                    dishOrders.add(saved);
                }else{
                    dishOrders = new HashSet<>();
                    dishOrders.add(saved);
                }
            }
        order.setDishOrders(dishOrders);
        orderRepository.save(order);
        return results;
    }

    @Override
    public List<DishOrderResponse> findDishByOrderId(Long orderId) {
        return dishOrderRepository.findDishOrderByOrder_Id(orderId).stream().map(dishOrderMapper::toDishOrderResponse).toList();
    }

    @Override
    public List<DishOrderResponse> findDishByOrderId(Long orderId, Pageable pageable) {
        return  dishOrderRepository.findDishOrderByOrder_Id(orderId,pageable).stream().map(dishOrderMapper::toDishOrderResponse).toList();
    }

    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public List<Order> findOrderByRestaurantId(Long restaurantId) {
        return orderRepository.findOrderByRestaurant_Id(restaurantId);
    }

    @Override
    public OrderResponse findOrderByTableId(Long tableId) {
        TableRestaurant tableRestaurant = tableRestaurantService.findById(tableId);
        if(tableRestaurant.getOrderCurrent() != null){
            Order order = findOrderById(tableRestaurant.getOrderCurrent());
            return orderMapper.toOrderResponse(order);
        }
        return null;
    }

    @Override
    public OrderResponse findOrderAndConvertDTOByOrderId(Long orderId) {
        Order order = findOrderById(orderId);
        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        Restaurant restaurant = restaurantService.getRestaurantById(order.getRestaurant().getId());
        double totalMoney = 0;
        int count = 0;
        for (DishOrder dishOrder : order.getDishOrders()){
            if(dishOrder.getStatus() != DISH_ORDER_STATE.DECLINE){
                if(dishOrder.getDish() != null){
                    totalMoney +=  (dishOrder.getDish().getPrice() * dishOrder.getQuantity());
                }else{
                    totalMoney +=  (dishOrder.getCombo().getPrice() * dishOrder.getQuantity());
                }
                count += dishOrder.getQuantity();
            }
        }
        if(restaurant.isVatActive()){
            if(restaurant.getVat() != null) {
                totalMoney += (totalMoney * (restaurant.getVat().getTaxValue() / 100));
            }

        }
        orderResponse.setTotalMoney(Math.round(totalMoney));
        orderResponse.setTotalDish(count);
        return orderResponse;
    }

    @Override
    public Long createOrder(Customer customer, Employee employee, TableRestaurant table, Restaurant restaurant) {
        Order order = Order.builder()
                .orderDate(LocalDate.now())
                .customer(customer)
                .restaurant(restaurant)
                .tableRestaurant(table)
                .employee(employee)
                .build();
        Long id = orderRepository.save(order).getId();
        table.setOrderCurrent(id);
        tableRestaurantRepository.save(table);
        return id;
    }
}
