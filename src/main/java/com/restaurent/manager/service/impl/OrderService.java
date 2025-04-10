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
    // Repositories and services for handling various operations
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

    /**
     * Creates a new order and associates it with a table, customer, and employee.
     *
     * @param request The order creation request.
     * @return The created order as a response DTO.
     */
    @Override
    public OrderResponse createOrder(OrderRequest request) {
        // Find the table by its ID
        TableRestaurant tableRestaurant = tableRestaurantService.findById(request.getTableId());

        // Create a new order and set its properties
        Order order = new Order();
        Customer customer = customerService.findCustomerByPhoneNumber(request.getCustomerResponse().getPhoneNumber(), request.getRestaurantId());
        order.setCustomer(customer);
        order.setRestaurant(restaurantService.getRestaurantById(request.getRestaurantId()));
        order.setTableRestaurant(tableRestaurant);
        order.setEmployee(employeeService.findEmployeeById(request.getEmployeeId()));
        order.setOrderDate(LocalDate.now());

        // Save the order and update the table's current order
        Order orderSaved = orderRepository.save(order);
        tableRestaurant.setOrderCurrent(orderSaved.getId());
        tableRestaurantRepository.save(tableRestaurant);

        // Return the created order as a response
        return orderMapper.toOrderResponse(orderSaved);
    }

    /**
     * Adds dishes or combos to an existing order.
     *
     * @param orderId     The ID of the order.
     * @param requestList The list of dish order requests.
     * @return A list of responses for the added dishes.
     */
    @Override
    public List<DishOrderResponse> addDishToOrder(Long orderId, List<DishOrderRequest> requestList) {
        // Find the order by its ID
        Order order = findOrderById(orderId);

        // Get the current set of dish orders
        Set<DishOrder> dishOrders = order.getDishOrders();
        List<DishOrderResponse> results = new ArrayList<>();

        // Iterate through the dish order requests
        for (DishOrderRequest request : requestList) {
            // Map the request to a DishOrder entity
            DishOrder dishOrder = dishOrderMapper.toDishOrder(request);

            // Set the dish or combo based on the request
            if (request.getDishId() != null) {
                dishOrder.setDish(dishService.findByDishId(request.getDishId()));
            } else {
                dishOrder.setCombo(comboService.findComboById(request.getComboId()));
            }

            // Set the order, status, and order date
            dishOrder.setOrder(order);
            dishOrder.setStatus(DISH_ORDER_STATE.WAITING);
            dishOrder.setOrderDate(LocalDateTime.now());

            // Save the dish order and add it to the response list
            DishOrder saved = dishOrderRepository.save(dishOrder);
            results.add(dishOrderMapper.toDishOrderResponse(saved));

            // Add the saved dish order to the current set of dish orders
            if (dishOrders != null) {
                dishOrders.add(saved);
            } else {
                dishOrders = new HashSet<>();
                dishOrders.add(saved);
            }
        }

        // Update the order with the new dish orders and save it
        order.setDishOrders(dishOrders);
        orderRepository.save(order);

        // Return the list of responses for the added dishes
        return results;
    }

    /**
     * Finds all dishes associated with an order by its ID.
     *
     * @param orderId The ID of the order.
     * @return A list of dish order responses.
     */
    @Override
    public List<DishOrderResponse> findDishByOrderId(Long orderId) {
        return dishOrderRepository.findDishOrderByOrder_Id(orderId).stream().map(dishOrderMapper::toDishOrderResponse).toList();
    }

    /**
     * Finds paginated dishes associated with an order by its ID.
     *
     * @param orderId  The ID of the order.
     * @param pageable The pagination information.
     * @return A list of dish order responses.
     */
    @Override
    public List<DishOrderResponse> findDishByOrderId(Long orderId, Pageable pageable) {
        return dishOrderRepository.findDishOrderByOrder_Id(orderId, pageable).stream().map(dishOrderMapper::toDishOrderResponse).toList();
    }

    /**
     * Finds an order by its ID.
     *
     * @param orderId The ID of the order.
     * @return The found order entity.
     */
    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(
            () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    /**
     * Finds all orders associated with a restaurant by its ID.
     *
     * @param restaurantId The ID of the restaurant.
     * @return A list of orders.
     */
    @Override
    public List<Order> findOrderByRestaurantId(Long restaurantId) {
        return orderRepository.findOrderByRestaurant_Id(restaurantId);
    }

    /**
     * Finds the current order associated with a table by its ID.
     *
     * @param tableId The ID of the table.
     * @return The current order as a response DTO, or null if no order exists.
     */
    @Override
    public OrderResponse findOrderByTableId(Long tableId) {
        TableRestaurant tableRestaurant = tableRestaurantService.findById(tableId);
        if (tableRestaurant.getOrderCurrent() != null) {
            Order order = findOrderById(tableRestaurant.getOrderCurrent());
            return orderMapper.toOrderResponse(order);
        }
        return null;
    }

    /**
     * Finds an order by its ID and converts it to a response DTO.
     *
     * @param orderId The ID of the order.
     * @return The order as a response DTO.
     */
    @Override
    public OrderResponse findOrderAndConvertDTOByOrderId(Long orderId) {
        Order order = findOrderById(orderId);
        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        Restaurant restaurant = restaurantService.getRestaurantById(order.getRestaurant().getId());

        // Calculate the total money and dish count
        double totalMoney = 0;
        int count = 0;
        for (DishOrder dishOrder : order.getDishOrders()) {
            if (dishOrder.getStatus() != DISH_ORDER_STATE.DECLINE) {
                if (dishOrder.getDish() != null) {
                    totalMoney += (dishOrder.getDish().getPrice() * dishOrder.getQuantity());
                } else {
                    totalMoney += (dishOrder.getCombo().getPrice() * dishOrder.getQuantity());
                }
                count += dishOrder.getQuantity();
            }
        }

        // Add VAT if applicable
        if (restaurant.isVatActive() && restaurant.getVat() != null) {
            totalMoney += (totalMoney * (restaurant.getVat().getTaxValue() / 100));
        }

        // Set the total money and dish count in the response
        orderResponse.setTotalMoney(Math.round(totalMoney));
        orderResponse.setTotalDish(count);
        return orderResponse;
    }

    /**
     * Creates a new order with the given customer, employee, table, and restaurant.
     *
     * @param customer   The customer entity.
     * @param employee   The employee entity.
     * @param table      The table entity.
     * @param restaurant The restaurant entity.
     * @return The ID of the created order.
     */
    @Override
    public Long createOrder(Customer customer, Employee employee, TableRestaurant table, Restaurant restaurant) {
        // Create a new order and set its properties
        Order order = Order.builder()
            .orderDate(LocalDate.now())
            .customer(customer)
            .restaurant(restaurant)
            .tableRestaurant(table)
            .employee(employee)
            .build();

        // Save the order and update the table's current order
        Long id = orderRepository.save(order).getId();
        table.setOrderCurrent(id);
        tableRestaurantRepository.save(table);

        // Return the ID of the created order
        return id;
    }
}