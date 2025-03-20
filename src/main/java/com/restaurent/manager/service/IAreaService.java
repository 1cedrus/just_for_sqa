package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.AreaRequest;
import com.restaurent.manager.dto.response.AreaResponse;

import java.util.List;

public interface IAreaService {
    AreaResponse createArea(AreaRequest request);
    List<AreaResponse> getAreasByRestaurantId(Long restaurantId);
    AreaResponse updateArea(Long areaId, AreaRequest request);
}
