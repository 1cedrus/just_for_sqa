package com.restaurent.manager.controller;

import com.restaurent.manager.dto.request.BillRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.BillResponse;
import com.restaurent.manager.dto.response.order.DishOrderResponse;
import com.restaurent.manager.service.IBillService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(makeFinal = true)
@RequestMapping(value = "/api/bill")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BillController {
    IBillService billService;
    @PreAuthorize(value = "hasRole('WAITER')")
    @PostMapping(value = "/create/order/{orderId}")
    public ApiResponse<BillResponse> createBill(@PathVariable Long orderId, @RequestBody @Valid BillRequest request){
        return ApiResponse.<BillResponse>builder()
                .result(billService.createBill(orderId,request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('BILL')")
    @GetMapping(value = "/restaurant/{restaurantId}")
    public ApiResponse<List<BillResponse>> getBillsByRestaurantId(@PathVariable Long restaurantId, @RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<List<BillResponse>>builder()
                .result(billService.getBillsByRestaurantId(restaurantId,pageable))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('BILL')")
    @GetMapping(value = "/{billId}")
    public ApiResponse<List<DishOrderResponse>> getDetailBillByBillId(@PathVariable Long billId,@RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<List<DishOrderResponse>>builder()
                .result(billService.getDetailBillByBillId(billId,pageable))
                .build();
    }
}
