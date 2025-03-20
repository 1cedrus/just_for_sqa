package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.order.OrderRequest;
import com.restaurent.manager.dto.response.order.OrderResponse;
import com.restaurent.manager.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface OrderMapper {
    Order toOrder(OrderRequest request);
    OrderResponse toOrderResponse(Order order);
}
