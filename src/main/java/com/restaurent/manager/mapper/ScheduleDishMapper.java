package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.response.ScheduleDishResponse;
import com.restaurent.manager.entity.ScheduleDish;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface ScheduleDishMapper {
    ScheduleDishResponse toScheduleResponse(ScheduleDish scheduleDish);
}
