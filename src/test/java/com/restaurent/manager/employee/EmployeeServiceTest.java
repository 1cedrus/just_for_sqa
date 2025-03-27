package com.restaurent.manager.employee;

import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.response.EmployeeResponse;
import com.restaurent.manager.entity.Employee;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.EmployeeMapper;
import com.restaurent.manager.repository.EmployeeRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.RoleRepository;
import com.restaurent.manager.service.IAccountService;
import com.restaurent.manager.service.impl.EmployeeService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    private EmployeeService employeeService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private IAccountService accountService;

    private PasswordEncoder passwordEncoder;
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // Ensure mocks are initialized
        employeeService = new EmployeeService(restaurantRepository, roleRepository, employeeMapper, employeeRepository, accountService);
        passwordEncoder = new BCryptPasswordEncoder(10);
    }

    @Test
    void createEmployee_TestChuan() {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
            .accountId(1L)
            .employeeName("test")
            .phoneNumber("0123456789")
            .roleId(1L)
            .username("testUser")
            .password("password")
            .build();

        Restaurant restaurant = Restaurant.builder()
            .id(employeeRequest.getAccountId())
            .restaurantName("My Restaurant")
            .employees(new HashSet<>())
            .build();

        Role role = Role.builder()
            .id(employeeRequest.getRoleId())
            .employees(new HashSet<>())
            .build();

        Employee employeeBeforeSave = Employee.builder()
                .employeeName(employeeRequest.getEmployeeName())
                .username(employeeRequest.getUsername())
                .password(employeeRequest.getPassword())
                .build();

        Employee employeeAfterSave = Employee.builder()
                .employeeName(employeeRequest.getEmployeeName())
                .username(employeeRequest.getUsername())
                .password(passwordEncoder.encode(employeeRequest.getPassword()))
                .build();

        EmployeeResponse employeeResponse = EmployeeResponse.builder()
            .employeeName(employeeAfterSave.getEmployeeName())
            .username(employeeAfterSave.getUsername())
            .password(employeeAfterSave.getPassword())
            .phoneNumber(employeeAfterSave.getPhoneNumber())
            .build();

        Mockito.when(restaurantRepository.findByAccount_Id(employeeRequest.getAccountId()))
            .thenReturn(restaurant);

        Mockito.when(employeeRepository.existsByUsernameAndRestaurant_Id(
                employeeRequest.getUsername(), restaurant.getId()))
            .thenReturn(false);

        Mockito.when(employeeMapper.toEmployee(employeeRequest))
            .thenReturn(employeeBeforeSave);

        Mockito.when(roleRepository.findById(employeeRequest.getRoleId()))
            .thenReturn(Optional.of(role));

        Mockito.when(employeeRepository.save(employeeBeforeSave))
            .thenReturn(employeeAfterSave);
        Mockito.when(employeeMapper.toEmployeeResponse(ArgumentMatchers.any(Employee.class))).thenReturn(employeeResponse);

        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("test", result.getEmployeeName());
        Mockito.verify(employeeRepository, Mockito.times(1)).save(employeeBeforeSave);
        Assertions.assertEquals(1, restaurant.getEmployees().size());
        Assertions.assertEquals(1, role.getEmployees().size());
    }

    @Test
    void findEmployeeById_TestChuan() {
        Employee employee = Employee.builder()
                .id(1L)
                .employeeName("test")
                .username("testUser")
                .password("password")
                .role(new Role())
                .build();

        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var result = employeeService.findEmployeeById(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals("test", result.getEmployeeName());
        Assertions.assertEquals("testUser", result.getUsername());
        Assertions.assertEquals("password", result.getPassword());
        Assertions.assertNotNull(result.getRole());
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void findEmployeeById_NgoaiLe1() {
        Mockito.when(employeeRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        AppException appException = Assertions.assertThrows(AppException.class, () -> employeeService.findEmployeeById(ArgumentMatchers.anyLong()));
        Assertions.assertEquals(ErrorCode.USER_NOT_EXISTED, appException.getErrorCode());
    }
}
