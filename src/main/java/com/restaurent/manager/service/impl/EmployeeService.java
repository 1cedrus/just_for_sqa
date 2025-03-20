package com.restaurent.manager.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.employee.EmployeeLoginRequest;
import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateInformationRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateRequest;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.EmployeeResponse;
import com.restaurent.manager.entity.Account;
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
import com.restaurent.manager.service.IEmployeeService;
import com.restaurent.manager.service.ITokenGenerate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class EmployeeService implements IEmployeeService, ITokenGenerate<Employee> {
    @NonFinal
    @Value("${jwt.signerKey}")
    String signerKey;
    RestaurantRepository restaurantRepository;
    RoleRepository roleRepository;
    EmployeeMapper employeeMapper;
    EmployeeRepository employeeRepository;
    IAccountService accountService;
    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Restaurant restaurant = restaurantRepository.findByAccount_Id(request.getAccountId());
        if(employeeRepository.existsByUsernameAndRestaurant_Id(request.getUsername(),restaurant.getId())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Employee employee = employeeMapper.toEmployee(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        Role role = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)
        );
        restaurant.addEmployee(employee);
        role.assginEmployee(employee);
        employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateInformationRequest request) {
        Employee employee = findEmployeeById(employeeId);
        employeeMapper.updateRestaurant(employee,request);
        employee.setRole(roleRepository.findById(request.getRoleId()).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        ));
        employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    public Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
    }

    @Override
    public EmployeeResponse findEmployeeByIdConvertDTO(Long id) {
        return employeeMapper.toEmployeeResponse(findEmployeeById(id));
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        employeeRepository.delete(employee);
    }

    @Override
    public PagingResult<EmployeeResponse> findEmployeesByAccountId(Long accountId, Pageable pageable, String query) {
        Restaurant restaurant = restaurantRepository.findByAccount_Id(accountId);
        if(restaurant == null){
            throw new AppException(ErrorCode.NOT_EXIST);
        }
        return PagingResult.<EmployeeResponse>builder()
                .results(employeeRepository.findByRestaurant_IdAndEmployeeNameContaining(restaurant.getId(),query,pageable).stream().map(employeeMapper::toEmployeeResponse).toList())
                .totalItems(employeeRepository.countByRestaurant_IdAndEmployeeNameContaining(restaurant.getId(),query))
                .build();
    }

    @Override
    public AuthenticationResponse authenticated(EmployeeLoginRequest request) {
        Account account = accountService.findAccountByPhoneNumber(request.getPhoneNumberOfRestaurant());
        Employee employee = employeeRepository.findByUsernameAndRestaurant_Id(request.getUsername(),account.getRestaurant().getId()).orElseThrow(
                () -> new AppException(ErrorCode.EMPLOYEE_NOT_EXIST)
        );
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(),employee.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        String token = generateToken(employee);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    @Override
    public void updateEmployeePassword(Long employeeId, String newPassword) {
        Employee employee = findEmployeeById(employeeId);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }

    @Override
    public String buildScope(Employee employee) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(employee.getRole() != null){
            stringJoiner.add("ROLE_" + employee.getRole().getName());
            if(employee.getRestaurant() != null){
                employee.getRestaurant().getRestaurantPackage().getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            }
        }
        return stringJoiner.toString();
    }

    @Override
    public String generateToken(Employee employee) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(employee.getEmployeeName())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(4, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope",buildScope(employee))
                .claim("restaurantId",employee.getRestaurant().getId())
                .claim("employeeId",employee.getId())
                .claim("accountId",employee.getRestaurant().getAccount().getId())
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(claims.toJSONObject());
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWSObject token = new JWSObject(header,payload);
        try {
            token.sign(new MACSigner(signerKey.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        return token.serialize();
    }
}
