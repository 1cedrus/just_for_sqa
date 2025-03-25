package com.restaurent.manager.employee;

import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.response.EmployeeResponse;
import com.restaurent.manager.entity.Employee;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.mapper.EmployeeMapper;
import com.restaurent.manager.repository.EmployeeRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.RoleRepository;
import com.restaurent.manager.service.IAccountService;
import com.restaurent.manager.service.impl.EmployeeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private IAccountService accountService;

    @BeforeEach
    public void setUP() {
        ReflectionTestUtils.setField(employeeService, "signerKey", "abc");
    }

    @Test
    void createEmployeeTest() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        Mockito.lenient().doReturn(restaurant).when(restaurantRepository).findByAccount_Id(ArgumentMatchers.anyLong());

        Mockito.lenient().doReturn(false).when(employeeRepository).existsByUsernameAndRestaurant_Id(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());

        Employee employee = new Employee();
        Mockito.lenient().doReturn(employee).when(employeeMapper).toEmployee(ArgumentMatchers.any());

        Role role = new Role();
        Mockito.lenient().doReturn(Optional.of(role)).when(roleRepository).findById(ArgumentMatchers.anyLong());

        EmployeeResponse employeeResponse = new EmployeeResponse();
        Mockito.lenient().doReturn(employeeResponse).when(employeeMapper).toEmployeeResponse(employee);

        Assertions.assertDoesNotThrow(() -> employeeService.createEmployee(new EmployeeRequest()));
    }
}
