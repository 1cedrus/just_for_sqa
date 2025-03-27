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

        // ✅ Restaurant mock
        Restaurant restaurant = Restaurant.builder()
            .id(1L)
            .restaurantName("My Restaurant")
            .employees(new HashSet<>())
            .build();

        // ✅ Employee mock
        Employee employee = Employee.builder()
            .employeeName("test")
            .username("testUser")
            .password("encodedPassword")  // Mật khẩu mã hóa
            .build();

        // ✅ Role mock
        Role role = Role.builder()
            .id(1L)
            .employees(new HashSet<>())
            .build();

        EmployeeResponse employeeResponse = EmployeeResponse.builder()
            .employeeName(employee.getEmployeeName())
            .username(employee.getUsername())
            .password(passwordEncoder.encode(employee.getPassword()))
            .phoneNumber(employee.getPhoneNumber())
            .build();

        // 📌 Khi gọi findByAccount_Id -> trả về Restaurant
        Mockito.when(restaurantRepository.findByAccount_Id(employeeRequest.getAccountId()))
            .thenReturn(restaurant);

        // 📌 Khi gọi existsByUsernameAndRestaurant_Id -> trả về false (username chưa tồn tại)
        Mockito.when(employeeRepository.existsByUsernameAndRestaurant_Id(
                employeeRequest.getUsername(), restaurant.getId()))
            .thenReturn(false);

        // 📌 Khi gọi mapper -> trả về Employee
        Mockito.when(employeeMapper.toEmployee(employeeRequest))
            .thenReturn(employee);

        // 📌 Khi gọi roleRepository.findById -> trả về Role
        Mockito.when(roleRepository.findById(employeeRequest.getRoleId()))
            .thenReturn(Optional.of(role));

        // 📌 Khi gọi save, trả về employee đã lưu
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class)))
            .thenReturn(employee);

        Mockito.when(employeeMapper.toEmployeeResponse(Mockito.any(Employee.class))).thenReturn(employeeResponse);

        // 🛠 **Gọi service**
        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        // ✅ **Kiểm tra kết quả**
        Assertions.assertNotNull(result);
        Assertions.assertEquals("test", result.getEmployeeName());

        // ✅ Kiểm tra mật khẩu đã được mã hóa
        Assertions.assertTrue(passwordEncoder.matches(employee.getPassword(), result.getPassword()));

        // ✅ Kiểm tra save được gọi
        Mockito.verify(employeeRepository, Mockito.times(1)).save(employee);

        // ✅ Kiểm tra employee được thêm vào restaurant
        Assertions.assertTrue(restaurant.getEmployees().contains(employee));

        // ✅ Kiểm tra role được gán đúng
        Assertions.assertTrue(role.getEmployees().contains(employee));
    }
}
