package com.restaurent.manager.controller;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.Customer.CustomerRequest;
import com.restaurent.manager.dto.request.Customer.CustomerUpdateRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.CustomerResponse;
import com.restaurent.manager.service.ICustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER') and hasAuthority('CUSTOMER')")
    @PostMapping("create")
    public ApiResponse<CustomerResponse> createCustomer(@RequestBody @Valid CustomerRequest customerRequest) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.createCustomer(customerRequest))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER') and hasAuthority('CUSTOMER')")
    @PutMapping("update")
    public ApiResponse<CustomerResponse> updateCustomer(@RequestBody @Valid CustomerUpdateRequest customerUpdateRequest) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.updateCustomer(customerUpdateRequest))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER', 'HOSTESS') and hasAuthority('CUSTOMER')")

    @GetMapping("getDetail/{id}")
    public ApiResponse<CustomerResponse> getCustomerDetail(@PathVariable long id) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getCustomerById(id))
                .build();
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER') and hasAuthority('CUSTOMER')")
    @GetMapping("/rankingCustomer/{restaurantID}")
    public PagingResult<CustomerResponse> getCustomersOrderByTotalPoint(@PathVariable long restaurantID, @RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size,
                                                                        @RequestParam(value = "query",defaultValue = "") String query) {
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return customerService.getCustomersOrderByTotalPoint(restaurantID,pageable,query);
    }
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'WAITER') and hasAuthority('CUSTOMER')")
    @GetMapping(value = "/restaurant/{restaurantId}/{phoneNumber}")
    public ApiResponse<CustomerResponse> findCustomerByPhoneNumberAndRestaurant(@PathVariable String phoneNumber, @PathVariable Long restaurantId){
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.findCustomerResponseByPhoneNumber(phoneNumber,restaurantId))
                .build();
    }


}
