package com.restaurent.manager.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.restaurent.manager.service.impl.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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




@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private Pageable pageable;

    private CustomerRequest customerRequest;
    private Restaurant restaurant;
    private Customer customer;
    private CustomerResponse customerResponse;
    private CustomerUpdateRequest updateRequest;

    @BeforeEach
    void setup() {
        customerRequest = new CustomerRequest();
        customerRequest.setPhoneNumber("0901234567");
        customerRequest.setName("John Doe");
        customerRequest.setAddress("123 Street");
        customerRequest.setRestaurantId(1L);

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setRestaurantName("Test Restaurant");
        customer = new Customer();
        customer.setPhoneNumber("0901234567");
        customer.setName("John Doe");
        customer.setAddress("123 Street");
        customer.setRestaurant(restaurant);

        customerResponse = new CustomerResponse();
        customerResponse.setId(1L);
        customerResponse.setPhoneNumber("0901234567");
        customerResponse.setName("John Doe");
        customerResponse.setAddress("123 Street");
        customerResponse.setRestaurantName("Test Restaurant");

        updateRequest = CustomerUpdateRequest.builder()
                .id(1L)
                .phoneNumber("0901234567")
                .name("Updated Name")
                .address("New Address")
                .restaurantId(1L)
                .build();
    }

    //CS1
    @Test
    void createCustomer_ShouldReturnCustomerResponse_WhenCustomerIsCreatedSuccessfully() {
        // given
        when(customerRepository.findByPhoneNumberAndRestaurantId("0901234567", 1L))
                .thenReturn(Optional.empty());
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));
        when(customerMapper.toCustomer(customerRequest))
                .thenReturn(customer);
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);
        when(customerMapper.toCustomerResponse(customer))
                .thenReturn(customerResponse);

        // when
        CustomerResponse result = customerService.createCustomer(customerRequest);

        // then
        assertNotNull(result);
        assertEquals("0901234567", result.getPhoneNumber());
        verify(customerRepository).save(any(Customer.class));
    }

    //CS2
    @Test
    void createCustomer_ShouldThrowException_WhenPhoneNumberAlreadyExists() {
        // given
        when(customerRepository.findByPhoneNumberAndRestaurantId("0901234567", 1L))
                .thenReturn(Optional.of(customer));

        // when & then
        AppException exception = assertThrows(AppException.class,
                () -> customerService.createCustomer(customerRequest));
        assertEquals(ErrorCode.PHONENUMBER_EXIST, exception.getErrorCode());

        verify(customerRepository, never()).save(any());
    }

    //CS3
    @Test
    void updateCustomer_ShouldUpdateSuccessfully_WhenValidData() {
        // given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByPhoneNumberAndRestaurantId("0901234567", 1L))
                .thenReturn(Optional.empty());
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toCustomerResponse(customer)).thenReturn(customerResponse);

        // when
        CustomerResponse result = customerService.updateCustomer(updateRequest);

        // then
        assertNotNull(result);
        assertEquals("0901234567", result.getPhoneNumber());
        verify(customerRepository).save(any(Customer.class));
    }

    //CS4
    @Test
    void updateCustomer_ShouldThrowException_WhenCustomerNotFound() {
        // given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        AppException ex = assertThrows(AppException.class,
                () -> customerService.updateCustomer(updateRequest));
        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
        verify(customerRepository, never()).save(any());
    }

    //CS5
    @Test
    void updateCustomer_ShouldThrowException_WhenPhoneNumberAlreadyExistsForAnotherCustomer() {
        // given
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L); // khác ID
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByPhoneNumberAndRestaurantId("0901234567", 1L))
                .thenReturn(Optional.of(otherCustomer));

        // when & then
        AppException ex = assertThrows(AppException.class,
                () -> customerService.updateCustomer(updateRequest));
        assertEquals(ErrorCode.USER_EXISTED, ex.getErrorCode());
        verify(customerRepository, never()).save(any());
    }

    //CS6
    @Test
    void getCustomerById_ShouldReturnCustomerResponse_WhenCustomerExists() {
        // given
        Long id = 1L;
        Customer customer = new Customer();
        customer.setId(id);
        customer.setPhoneNumber("0901234567");
        customer.setName("Test Name");

        CustomerResponse response = new CustomerResponse();
        response.setId(id);
        response.setPhoneNumber("0901234567");
        response.setName("Test Name");

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerMapper.toCustomerResponse(customer)).thenReturn(response);

        // when
        CustomerResponse result = customerService.getCustomerById(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("0901234567", result.getPhoneNumber());
        verify(customerRepository).findById(id);
        verify(customerMapper).toCustomerResponse(customer);
    }

    //CS7
    @Test
    void getCustomerById_ShouldThrowException_WhenCustomerDoesNotExist() {
        // given
        Long id = 2L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        AppException exception = assertThrows(AppException.class,
                () -> customerService.getCustomerById(id));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(customerRepository).findById(id);
        verify(customerMapper, never()).toCustomerResponse(any());
    }

    private Customer createCustomer(Long id, String name, String phone) {
        Customer c = new Customer();
        c.setId(id);
        c.setName(name);
        c.setPhoneNumber(phone);
        return c;
    }

    private CustomerResponse createCustomerResponse(Long id, String name, String phone) {
        CustomerResponse r = new CustomerResponse();
        r.setId(id);
        r.setName(name);
        r.setPhoneNumber(phone);
        return r;
    }

    //CS8
    @Test
    void getCustomersOrderByTotalPoint_ShouldReturnResults_WhenQueryIsEmpty() {
        // given
        String query = "";
        Long restaurantId = 1L;

        List<Customer> customerList = List.of(createCustomer(1L, "John", "0901234567"));
        when(customerRepository.findByRestaurant_IdAndNameContainingOrderByTotalPointDesc(restaurantId, query, pageable))
                .thenReturn(customerList);
        when(customerRepository.countByRestaurant_IdAndNameContaining(restaurantId, query)).thenReturn((int) 1L);
        when(customerMapper.toCustomerResponse(any())).thenReturn(createCustomerResponse(1L, "John", "0901234567"));

        // when
        PagingResult<CustomerResponse> result = customerService.getCustomersOrderByTotalPoint(restaurantId, pageable, query);

        // then
        assertEquals(1, result.getResults().size());
        assertEquals(1, result.getTotalItems());
    }

    //CS9
    @Test
    void findCustomerResponseByPhoneNumber_ShouldReturnResponse_WhenCustomerExists() {
        // given
        String phoneNumber = "0901234567";
        Long restaurantId = 1L;
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setPhoneNumber(phoneNumber);
        customer.setName("John");

        CustomerResponse expectedResponse = new CustomerResponse();
        expectedResponse.setId(1L);
        expectedResponse.setPhoneNumber(phoneNumber);
        expectedResponse.setName("John");

        when(customerRepository.findByPhoneNumberAndRestaurant_Id(phoneNumber, restaurantId))
                .thenReturn(Optional.of(customer));
        when(customerMapper.toCustomerResponse(customer)).thenReturn(expectedResponse);

        // when
        CustomerResponse result = customerService.findCustomerResponseByPhoneNumber(phoneNumber, restaurantId);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(expectedResponse.getName(), result.getName());
    }

    //CS10
    @Test
    void findCustomerResponseByPhoneNumber_ShouldThrowException_WhenCustomerNotExists() {
        // given
        String phoneNumber = "0901234567";
        Long restaurantId = 1L;

        when(customerRepository.findByPhoneNumberAndRestaurant_Id(phoneNumber, restaurantId))
                .thenReturn(Optional.empty());

        // when & then
        AppException ex = assertThrows(AppException.class, () -> {
            customerService.findCustomerResponseByPhoneNumber(phoneNumber, restaurantId);
        });

        assertEquals(ErrorCode.CUSTOMER_NOT_EXIST, ex.getErrorCode());
    }

    //CS11
    @Test
    void findCustomerByPhoneNumber_ShouldReturnCustomer_WhenExists() {
        // given
        String phoneNumber = "0901234567";
        Long restaurantId = 1L;

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setPhoneNumber(phoneNumber);
        customer.setName("John");

        when(customerRepository.findByPhoneNumberAndRestaurant_Id(phoneNumber, restaurantId))
                .thenReturn(Optional.of(customer));

        // when
        Customer result = customerService.findCustomerByPhoneNumber(phoneNumber, restaurantId);

        // then
        assertNotNull(result);
        assertEquals(phoneNumber, result.getPhoneNumber());
        assertEquals("John", result.getName());
    }

    //CS12
    @Test
    void findCustomerByPhoneNumber_ShouldThrowException_WhenNotExists() {
        // given
        String phoneNumber = "0900000000";
        Long restaurantId = 2L;

        when(customerRepository.findByPhoneNumberAndRestaurant_Id(phoneNumber, restaurantId))
                .thenReturn(Optional.empty());

        // when & then
        AppException ex = assertThrows(AppException.class, () ->
                customerService.findCustomerByPhoneNumber(phoneNumber, restaurantId)
        );

        assertEquals(ErrorCode.CUSTOMER_NOT_EXIST, ex.getErrorCode());
    }

    //CS13
    @Test
    void existCustomerByPhoneNumberAndRestaurantId_ShouldReturnTrue_WhenCustomerExists() {
        // given
        String phoneNumber = "0901234567";
        Long restaurantId = 1L;

        when(customerRepository.existsByPhoneNumberAndRestaurant_Id(phoneNumber, restaurantId))
                .thenReturn(true);

        // when
        boolean result = customerService.existCustomerByPhoneNumberAndRestaurantId(phoneNumber, restaurantId);

        // then
        assertTrue(result);
    }

    //CS14
    @Test
    void existCustomerByPhoneNumberAndRestaurantId_ShouldReturnFalse_WhenCustomerDoesNotExist() {
        // given
        String phoneNumber = "0900000000";
        Long restaurantId = 2L;

        when(customerRepository.existsByPhoneNumberAndRestaurant_Id(phoneNumber, restaurantId))
                .thenReturn(false);

        // when
        boolean result = customerService.existCustomerByPhoneNumberAndRestaurantId(phoneNumber, restaurantId);

        // then
        assertFalse(result);
    }

}

