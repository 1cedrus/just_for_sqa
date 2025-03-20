package com.restaurent.manager.controller;

import com.restaurent.manager.dto.request.AreaRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.AreaResponse;
import com.restaurent.manager.service.IAreaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api/area")
public class AreaController {
    IAreaService areaService;

    @GetMapping(value = "/{restaurantId}")
    public ApiResponse<List<AreaResponse>> getAreasByRestaurantId(@PathVariable Long restaurantId){
        return ApiResponse.<List<AreaResponse>>builder()
                .result(areaService.getAreasByRestaurantId(restaurantId))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('AREA')")
    @PostMapping(value = "/create")
    public ApiResponse<AreaResponse> createArea(@RequestBody @Valid AreaRequest request){
        return ApiResponse.<AreaResponse>builder()
                .result(areaService.createArea(request))
                .build();
    }
    @PutMapping(value = "/{areaId}")
    public ApiResponse<AreaResponse> updateArea(@PathVariable Long areaId, @RequestBody AreaRequest request){
        return ApiResponse.<AreaResponse>builder()
                .result(areaService.updateArea(areaId,request))
                .build();
    }
}
