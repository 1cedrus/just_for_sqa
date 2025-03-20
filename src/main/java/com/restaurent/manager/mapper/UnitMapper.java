package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.UnitRequest;
import com.restaurent.manager.dto.response.UnitResponse;
import com.restaurent.manager.entity.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UnitMapper {
    Unit toUnit(UnitRequest request);
    UnitResponse toUnitResponse(Unit unit);
    void updateUnit(@MappingTarget Unit unit, UnitRequest request);
}
