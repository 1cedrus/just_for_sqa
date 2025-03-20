package com.restaurent.manager.controller;

import com.restaurent.manager.dto.request.RestaurantPackagePaymentHistoryRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.service.IRestaurantPackagePaymentHistoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/package-history")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
public class PackageHistoryController {
    IRestaurantPackagePaymentHistoryService service;
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PostMapping(value = "/create")
    public ApiResponse<Long> create(@RequestBody @Valid RestaurantPackagePaymentHistoryRequest request){

        return ApiResponse.<Long>builder()
                .message("create success")
                .result(service.createRestaurantPackagePaymentHistory(request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @GetMapping(value = "/new-id")
    public ApiResponse<Long> getNewID(){
        return ApiResponse.<Long>builder()
                .result(service.getNewId())
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PutMapping(value = "/{packHistoryId}/success")
    public ApiResponse<String> restaurantPaidPack(@PathVariable Long packHistoryId, @RequestBody RestaurantPackagePaymentHistoryRequest request){
        return ApiResponse.<String>builder()
                .result(service.updateRestaurantPackagePaymentHistory(packHistoryId, request))
                .build();
    }

}
