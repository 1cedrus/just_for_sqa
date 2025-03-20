package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.VatRequest;
import com.restaurent.manager.entity.Vat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")

public interface VatMapper {
    Vat toVat(VatRequest request);
    void updateVat(@MappingTarget Vat vat, VatRequest request);
}
