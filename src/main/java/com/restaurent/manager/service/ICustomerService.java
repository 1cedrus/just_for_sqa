package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.Customer.CustomerRequest;
import com.restaurent.manager.dto.request.Customer.CustomerUpdateRequest;
import com.restaurent.manager.dto.response.CustomerResponse;
import com.restaurent.manager.entity.Customer;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICustomerService {
    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(CustomerUpdateRequest request);

    CustomerResponse getCustomerById(Long id); // New method declaration

    PagingResult<CustomerResponse> getCustomersOrderByTotalPoint(Long id, Pageable pageable, String query);
    CustomerResponse findCustomerResponseByPhoneNumber(String phoneNumber, Long restaurantId);
    Customer findCustomerByPhoneNumber(String phoneNumber, Long restaurantId);
    boolean existCustomerByPhoneNumberAndRestaurantId(String phone,Long restaurantId);
}
