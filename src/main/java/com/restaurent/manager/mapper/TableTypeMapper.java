package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.TableTypeRequest;
import com.restaurent.manager.dto.request.TableTypeUpdateRequest;
import com.restaurent.manager.dto.response.TableTypeResponse;
import com.restaurent.manager.entity.TableType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TableTypeMapper {
    TableType toTableType(TableTypeRequest request);
    TableTypeResponse toTableTypeResponse(TableType tableType);
    void updateRestaurant(@MappingTarget TableType tableType, TableTypeUpdateRequest request);
}
