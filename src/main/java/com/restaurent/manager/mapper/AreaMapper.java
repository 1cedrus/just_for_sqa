package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.AreaRequest;
import com.restaurent.manager.dto.response.AreaResponse;
import com.restaurent.manager.entity.Area;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AreaMapper {
    Area toArea(AreaRequest request);
    AreaResponse toAreaResponse(Area area);
}
