package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.RestaurantPackagePaymentHistoryRequest;
import com.restaurent.manager.dto.response.RestaurantPackageHistoryResponse;
import com.restaurent.manager.entity.RestaurantPackagePaymentHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantPackageHistoryMapper {
    RestaurantPackagePaymentHistory toRestaurantPackagePaymentHistory(RestaurantPackagePaymentHistoryRequest request);
    RestaurantPackageHistoryResponse toRestaurantPackageHistoryResponse(RestaurantPackagePaymentHistory RestaurantPackagePaymentHistory);
}
