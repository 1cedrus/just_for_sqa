package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.ScheduleRequest;
import com.restaurent.manager.dto.response.ScheduleResponse;
import com.restaurent.manager.entity.Schedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    Schedule toSchedule(ScheduleRequest request);
    ScheduleResponse toScheduleResponse(Schedule schedule);
}
