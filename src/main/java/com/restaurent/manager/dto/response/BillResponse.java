package com.restaurent.manager.dto.response;

import com.restaurent.manager.dto.response.order.OrderResponse;
import com.restaurent.manager.enums.MethodPayment;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillResponse {
    Long id;
    OrderResponse order;
    double total;
    LocalDateTime dateCreated;
    MethodPayment methodPayment;
}
