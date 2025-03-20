package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.Customer.CustomerRequest;
import com.restaurent.manager.dto.response.CustomerResponse;
import com.restaurent.manager.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(CustomerRequest request);
    CustomerResponse toCustomerResponse(Customer customer);
}
