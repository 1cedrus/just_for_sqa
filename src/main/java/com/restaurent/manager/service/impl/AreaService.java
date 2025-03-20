package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.AreaRequest;
import com.restaurent.manager.dto.response.AreaResponse;
import com.restaurent.manager.entity.Area;
import com.restaurent.manager.entity.Permission;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.AreaMapper;
import com.restaurent.manager.repository.AreaRepository;
import com.restaurent.manager.service.IAreaService;
import com.restaurent.manager.service.IRestaurantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class AreaService implements IAreaService {
    AreaRepository areaRepository;
    AreaMapper areaMapper;
    IRestaurantService restaurantService;
    @Override
    public AreaResponse createArea(AreaRequest request) {
        Area area = areaMapper.toArea(request);
        int totalArea = areaRepository.countByRestaurant_Id(request.getRestaurantId());
        Restaurant restaurant = restaurantService.getRestaurantById(request.getRestaurantId());
        for (Permission permission : restaurant.getRestaurantPackage().getPermissions()){
            if(permission.getName().equals("AREA_MAX")){
                int max = permission.getMaximum();
                if(totalArea >= max){
                    throw new AppException(ErrorCode.MAX_AREA);
                }
            }
        }
        area.setRestaurant(restaurant);
        return areaMapper.toAreaResponse(
                areaRepository.save(area)
        );
    }

    @Override
    public List<AreaResponse> getAreasByRestaurantId(Long restaurantId) {
        return areaRepository.findByRestaurant_Id(restaurantId).stream().map(areaMapper::toAreaResponse).toList();
    }

    @Override
    public AreaResponse updateArea(Long areaId, AreaRequest request) {
        Area area = areaRepository.findById(areaId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
        area.setName(request.getName());
        return areaMapper.toAreaResponse(areaRepository.save(area));
    }
}
