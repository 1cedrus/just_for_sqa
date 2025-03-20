package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.Customer.CustomerRequest;
import com.restaurent.manager.dto.request.Customer.CustomerUpdateRequest;
import com.restaurent.manager.dto.response.CustomerResponse;
import com.restaurent.manager.entity.Customer;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.CustomerMapper;
import com.restaurent.manager.repository.CustomerRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.service.ICustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class CustomerService implements ICustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    RestaurantRepository restaurantRepository;

    @Override
    public CustomerResponse createCustomer(CustomerRequest customerRequest) {
        Optional<Customer> existingCustomer = customerRepository.findByPhoneNumberAndRestaurantId(
                customerRequest.getPhoneNumber(), customerRequest.getRestaurantId());
        if (existingCustomer.isPresent()) {
            throw new AppException(ErrorCode.PHONENUMBER_EXIST);
        } else {
            // Fetch the Restaurant from the database
            Restaurant restaurant = restaurantRepository.findById(customerRequest.getRestaurantId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESTAURANT_NOT_EXISTED));

            Customer customer = customerMapper.toCustomer(customerRequest);
            customer.setRestaurant(restaurant); // Set the Restaurant for the Customer
            customer.setDateCreated(LocalDateTime.now());
            Customer savedCustomer = customerRepository.save(customer);
            return customerMapper.toCustomerResponse(savedCustomer);
        }
    }

    @Override
    public CustomerResponse updateCustomer(CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check for phone number uniqueness within the same restaurant
        Optional<Customer> otherCustomer = customerRepository.findByPhoneNumberAndRestaurantId(
                request.getPhoneNumber(), request.getRestaurantId());
        if (otherCustomer.isPresent() && !otherCustomer.get().getId().equals(request.getId())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Update customer details
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setName(request.getName());
        customer.setAddress(request.getAddress());

        if (request.getRestaurantId() != null) {
            // Fetch the Restaurant from the database
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESTAURANT_NOT_EXISTED));
            customer.setRestaurant(restaurant);
        }

        // Save updated customer
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toCustomerResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    public PagingResult<CustomerResponse> getCustomersOrderByTotalPoint(Long restaurantId, Pageable pageable, String query) {
        List<Customer> customers = customerRepository.findByRestaurant_IdAndNameContainingOrderByTotalPointDesc(restaurantId,query,pageable);
       if(!query.isEmpty()){
           List<Customer> customerPhone = customerRepository.findByRestaurant_IdAndPhoneNumberContainingOrderByTotalPointDesc(restaurantId,query,pageable);
           if(!customerPhone.isEmpty()){
               customers.addAll(customerPhone);
           }
       }
        return PagingResult.<CustomerResponse>builder()
                .results(customers.stream().map(customerMapper::toCustomerResponse).toList())
                .totalItems(customerRepository.countByRestaurant_IdAndNameContaining(restaurantId,query))
                .build();
    }

    @Override
    public CustomerResponse findCustomerResponseByPhoneNumber(String phoneNumber, Long restaurantId) {
        Customer customer = customerRepository.findByPhoneNumberAndRestaurant_Id(phoneNumber,restaurantId).orElseThrow(
                () -> new AppException(ErrorCode.CUSTOMER_NOT_EXIST)
        );
        return customerMapper.toCustomerResponse(customer);
    }
    @Override
    public Customer findCustomerByPhoneNumber(String phoneNumber, Long restaurantId) {
        Customer customer = customerRepository.findByPhoneNumberAndRestaurant_Id(phoneNumber,restaurantId).orElseThrow(
                () -> new AppException(ErrorCode.CUSTOMER_NOT_EXIST)
        );
        return customer;
    }

    @Override
    public boolean existCustomerByPhoneNumberAndRestaurantId(String phone, Long restaurantId) {
        return customerRepository.existsByPhoneNumberAndRestaurant_Id(phone,restaurantId);
    }

}
