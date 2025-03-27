package com.restaurent.manager.employee;

import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.response.EmployeeResponse;
import com.restaurent.manager.entity.Employee;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/sqa_database",
    "spring.datasource.username=custom_username",
    "spring.datasource.password=custom_password",
    "spring.jpa.hibernate.ddl-auto=update",
}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EmployeeServiceTestConfig.class)
@Transactional
class EmployeeServiceTest {

    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Mock
    private IAccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // Ensure mocks are initialized
        employeeService = new EmployeeService(restaurantRepository, roleRepository, employeeMapper, employeeRepository, accountService);
    }

    @Test
    void createEmployee_TestChuan() {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
            .accountId(2L)
            .employeeName("test")
            .phoneNumber("0123456789")
            .roleId(1L)
            .username("testUser")
            .password("password")
            .build();

        EmployeeResponse result = employeeService.createEmployee(employeeRequest);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("test", result.getEmployeeName());

        Employee e = employeeService.findEmployeeById(Long.valueOf(result.getId()));

        Assertions.assertNotNull(e);
        Assertions.assertEquals("test", e.getEmployeeName());
        Assertions.assertEquals("0123456789", e.getPhoneNumber());
        Assertions.assertEquals("testUser", e.getUsername());
        Assertions.assertTrue(passwordEncoder.matches("password", e.getPassword()));
    }
}
