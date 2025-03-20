package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.UnitRequest;
import com.restaurent.manager.dto.response.UnitResponse;
import com.restaurent.manager.entity.Unit;

import java.util.List;

public interface IUnitService {
    UnitResponse createUnit(UnitRequest request);
    List<UnitResponse> getUnitsByAccountId(Long accountId);
    UnitResponse updateUnit(Long unitId,UnitRequest request);
    void deleteUnitById(Long unitId);
    Unit findById(Long id);
}
