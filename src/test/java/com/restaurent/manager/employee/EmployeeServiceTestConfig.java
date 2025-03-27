package com.restaurent.manager.employee;

import com.restaurent.manager.mapper.EmployeeMapper;
import com.restaurent.manager.mapper.EmployeeMapperImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class EmployeeServiceTestConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public EmployeeMapper employeeMapper() {
        return new EmployeeMapperImpl();
    }
}