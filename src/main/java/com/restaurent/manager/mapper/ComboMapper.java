package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.Combo.ComboRequest;
import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.entity.Combo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComboMapper {

    @Mapping(target = "id", ignore = true)
    Combo toCombo(ComboRequest request);

    ComboResponse toComboResponse(Combo combo);
}
