package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.BillRequest;
import com.restaurent.manager.dto.response.BillResponse;
import com.restaurent.manager.entity.Bill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface BillMapper {
    Bill toBill(BillRequest request);
    BillResponse toBillResponse(Bill bill);
}
