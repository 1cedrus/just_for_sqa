package com.restaurent.manager.controller;

import com.restaurent.manager.dto.request.TaxRequest;
import com.restaurent.manager.dto.request.VatRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.entity.Vat;
import com.restaurent.manager.service.IVatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/vat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(makeFinal = true)
public class VatController {
    IVatService vatService;
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PostMapping(value = "/restaurant/{restaurantId}/create")
    public ApiResponse<Vat> createVatForRestaurant(@PathVariable Long restaurantId, @RequestBody @Valid VatRequest request){
        return ApiResponse.<Vat>builder()
                .result(vatService.createVat(restaurantId,request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PutMapping(value = "/{vatId}")
    public ApiResponse<Vat> updateVat(@PathVariable Long vatId, @RequestBody @Valid VatRequest request){
        return ApiResponse.<Vat>builder()
                .result(vatService.updateVatInformation(vatId,request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER')")
    @PutMapping(value = "/{vatId}/tax")
    public ApiResponse<Vat> updateTax(@PathVariable Long vatId, @RequestBody @Valid TaxRequest request){
        return ApiResponse.<Vat>builder()
                .result(vatService.updateTax(vatId,request))
                .build();
    }
}
