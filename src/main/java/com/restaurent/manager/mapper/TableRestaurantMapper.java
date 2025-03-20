package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.Table.TableRestaurantRequest;
import com.restaurent.manager.dto.request.Table.TableRestaurantUpdateRequest;
import com.restaurent.manager.dto.response.TableRestaurantResponse;
import com.restaurent.manager.entity.TableRestaurant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TableRestaurantMapper {
    TableRestaurant toTableRestaurant(TableRestaurantRequest request);
    TableRestaurantResponse toTableRestaurantResponse(TableRestaurant tableRestaurant);
    TableRestaurantRequest toTableRestaurantRequest(TableRestaurantUpdateRequest request);
    void updateTable(@MappingTarget TableRestaurant tableRestaurant,TableRestaurantRequest request);
}
