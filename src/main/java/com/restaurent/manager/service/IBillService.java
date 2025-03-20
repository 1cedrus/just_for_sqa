package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.BillRequest;
import com.restaurent.manager.dto.response.BillResponse;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.entity.Bill;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IBillService {
    BillResponse createBill(Long orderId,BillRequest request);
    List<BillResponse> getBillsByRestaurantId(Long restaurantId,Pageable pageable);
    List<DishOrderResponse> getDetailBillByBillId(Long billId,Pageable pageable);
    Bill findBillById(Long billId);
    double getProfitRestaurantByIdAndDate(Long resId, LocalDateTime date);
    double getProfitRestaurantByIdAndDateBetween(Long resId, LocalDateTime start, LocalDateTime end);
    double getVatValueForRestaurantCurrent(Long resId,LocalDateTime date);
    double getVatValueForRestaurantBetween(Long resId,LocalDateTime start, LocalDateTime end);
    double getTotalValueByTimeAndCurrentForRestaurant(Long restaurantId, String startTime, String endTime);
    double getTotalValueByTimeAndCurrentWeekForRestaurant(Long restaurantId, String startTime, String endTime);
    double getTotalValueByTimeAndCurrentMonthForRestaurant(Long restaurantId, String startTime, String endTime);

}
