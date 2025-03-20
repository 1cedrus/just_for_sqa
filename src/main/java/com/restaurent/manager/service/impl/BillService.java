package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.BillRequest;
import com.restaurent.manager.dto.response.BillResponse;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.BillMapper;
import com.restaurent.manager.repository.BillRepository;
import com.restaurent.manager.repository.CustomerRepository;
import com.restaurent.manager.repository.TableRestaurantRepository;
import com.restaurent.manager.service.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class BillService implements IBillService {
    IOrderService orderService;
    BillMapper billMapper;
    BillRepository billRepository;
    ITableRestaurantService tableRestaurantService;
    TableRestaurantRepository tableRestaurantRepository;
    IRestaurantService restaurantService;
    CustomerRepository customerRepository;
    @Override
    public BillResponse createBill(Long orderId, BillRequest request) {
        Order order = orderService.findOrderById(orderId);
        Customer customer = order.getCustomer();
        TableRestaurant tableRestaurant = tableRestaurantService.findById(order.getTableRestaurant().getId());
        tableRestaurant.setOrderCurrent(null);
        tableRestaurantRepository.save(tableRestaurant);
        // handle change bill request to bill
        Bill bill = billMapper.toBill(request);
        bill.setOrder(order);
        bill.setDateCreated(LocalDateTime.now());
        // handle adding point for customer
        Restaurant restaurant = order.getRestaurant();
        if(request.getPoints() != 0){
            // handle reduce point when apply using point
            bill.setPointUsed(request.getPoints());
            int points = (int) (customer.getCurrentPoint() - request.getPoints());
            if(points < 0){
                throw new AppException(ErrorCode.POINT_INVALID);
            }
            customer.setCurrentPoint(points);
        }else{
            // handle adding point when customer paid
            int points = (int) (request.getTotal() / restaurant.getMoneyToPoint());
            log.info("point : " + points + "money to point : " + restaurant.getMoneyToPoint() + " Total "  + request.getTotal());
            customer.setCurrentPoint(customer.getCurrentPoint() + points);
            customer.setTotalPoint(customer.getTotalPoint() + points);
        }
        customerRepository.save(customer);
        return billMapper.toBillResponse(billRepository.save(bill));
    }

    @Override
    public List<BillResponse> getBillsByRestaurantId(Long restaurantId, Pageable pageable) {
        return billRepository.findByOrder_Restaurant_Id(restaurantId, pageable).stream().map(
                billMapper::toBillResponse
        ).toList();
    }

    @Override
    public List<DishOrderResponse> getDetailBillByBillId(Long billId, Pageable pageable) {
        Bill bill = findBillById(billId);
        return orderService.findDishByOrderId(bill.getOrder().getId(), pageable);
    }

    @Override
    public Bill findBillById(Long billId) {
        return billRepository.findById(billId).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
    }

    @Override
    public double getProfitRestaurantByIdAndDate(Long resId, LocalDateTime date) {
        Date sqlDate = Date.valueOf(date.toLocalDate());
        List<Bill> bills = billRepository.findByDateCreated(resId, sqlDate);
        double results = 0;
        if (!bills.isEmpty()) {
            for (Bill bill : bills) {
                results += bill.getTotal();
            }
            return Math.round(results);
        }
        return 0;
    }

    @Override
    public double getProfitRestaurantByIdAndDateBetween(Long resId, LocalDateTime start, LocalDateTime end) {
        List<Bill> bills = billRepository.findByDateCreatedBetween(resId, start, end);
        double results = 0;
        if (!bills.isEmpty()) {
            for (Bill bill : bills) {
                results += bill.getTotal();
            }
            return Math.round(results);
        }
        return 0;
    }

    @Override
    public double getVatValueForRestaurantCurrent(Long resId, LocalDateTime date) {
        Date sqlDate = Date.valueOf(date.toLocalDate());
        List<Bill> bills = billRepository.findByDateCreated(resId, sqlDate);
        Restaurant restaurant = restaurantService.getRestaurantById(resId);
        double res = 0;
        if (!bills.isEmpty()) {
            if(restaurant.isVatActive() && restaurant.getVat() != null){
                for (Bill bill : bills) {
                    res += bill.getTotal() * (restaurant.getVat().getTaxValue() / (100 + restaurant.getVat().getTaxValue()));
                }
            }
            return Math.round(res);
        }
        return 0;
    }

    @Override
    public double getVatValueForRestaurantBetween(Long resId, LocalDateTime start, LocalDateTime end) {
        List<Bill> bills = billRepository.findByDateCreatedBetween(resId, start, end);
        Restaurant restaurant = restaurantService.getRestaurantById(resId);
        double res = 0;
        if (!bills.isEmpty()) {
            if(restaurant.isVatActive() && restaurant.getVat() != null){
                for (Bill bill : bills) {
                    res += bill.getTotal() * (restaurant.getVat().getTaxValue() / (100 + restaurant.getVat().getTaxValue()));
                }
            }
            return Math.round(res);
        }
        return 0;
    }

    @Override
    public double getTotalValueByTimeAndCurrentForRestaurant(Long restaurantId, String startTime, String endTime) {
        double res = 0;
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentDate(restaurantId,startTime,endTime);
        if(!bills.isEmpty()){
            for (Bill b : bills){
                res += b.getTotal();
            }
            return Math.round(res);
        }
        return 0;
    }

    @Override
    public double getTotalValueByTimeAndCurrentWeekForRestaurant(Long restaurantId, String startTime, String endTime) {
        double res = 0;
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentWeek(restaurantId,startTime,endTime);
        if(!bills.isEmpty()){
            for (Bill b : bills){
                res += b.getTotal();
            }
            return Math.round(res);
        }
        return 0;
    }

    @Override
    public double getTotalValueByTimeAndCurrentMonthForRestaurant(Long restaurantId, String startTime, String endTime) {
        double res = 0;
        List<Bill> bills = billRepository.findByTimeBetweenAndCurrentMonth(restaurantId,startTime,endTime);
        if(!bills.isEmpty()){
            for (Bill b : bills){
                res += b.getTotal();
            }
            return Math.round(res);
        }
        return 0;
    }

}



