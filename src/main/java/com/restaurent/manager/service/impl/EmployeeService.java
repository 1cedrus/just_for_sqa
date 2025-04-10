package com.restaurent.manager.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.employee.EmployeeLoginRequest;
import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateInformationRequest;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeService implements IEmployeeService, ITokenGenerate<Employee> {
    // Repository for accessing restaurant data
    RestaurantRepository restaurantRepository;

    // Repository for accessing role data
    RoleRepository roleRepository;

    // Mapper for converting between Employee entities and DTOs
    EmployeeMapper employeeMapper;

    // Repository for accessing employee data
    EmployeeRepository employeeRepository;

    // Service for handling account-related operations
    IAccountService accountService;

    // JWT signer key for token generation
    @NonFinal
    @Value("${jwt.signerKey}")
    String signerKey;

    /**
     * Creates a new employee and associates it with a restaurant and role.
     *
     * @param request The employee creation request.
     * @return The created employee as a response DTO.
     */
    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // Find the restaurant by account ID
        Restaurant restaurant = restaurantRepository.findByAccount_Id(request.getAccountId());

        // Check if the username already exists for the restaurant
        if (employeeRepository.existsByUsernameAndRestaurant_Id(request.getUsername(), restaurant.getId())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Map the request to an Employee entity
        Employee employee = employeeMapper.toEmployee(request);

        // Encode the password using BCrypt
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        // Find the role by ID or throw an exception if not found
        Role role = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        // Associate the employee with the restaurant and role
        restaurant.addEmployee(employee);
        role.assginEmployee(employee);

        // Save the employee and return the response
        employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponse(employee);
    }

    /**
     * Updates an existing employee's information.
     *
     * @param employeeId The ID of the employee to update.
     * @param request    The update request containing new information.
     * @return The updated employee as a response DTO.
     */
    @Override
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateInformationRequest request) {
        // Find the employee by ID
        Employee employee = findEmployeeById(employeeId);

        // Update the employee's information using the mapper
        employeeMapper.updateRestaurant(employee, request);

        // Update the employee's role
        employee.setRole(roleRepository.findById(request.getRoleId()).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST)));

        // Save the updated employee and return the response
        employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    /**
     * Finds an employee by ID.
     *
     * @param id The ID of the employee.
     * @return The found employee entity.
     */
    @Override
    public Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Finds an employee by ID and converts it to a response DTO.
     *
     * @param id The ID of the employee.
     * @return The employee as a response DTO.
     */
    @Override
    public EmployeeResponse findEmployeeByIdConvertDTO(Long id) {
        return employeeMapper.toEmployeeResponse(findEmployeeById(id));
    }

    /**
     * Deletes an employee by ID.
     *
     * @param id The ID of the employee to delete.
     */
    @Override
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        employeeRepository.delete(employee);
    }

    /**
     * Finds employees by account ID with pagination and search query.
     *
     * @param accountId The account ID.
     * @param pageable  The pagination information.
     * @param query     The search query.
     * @return A paginated result of employees.
     */
    @Override
    public PagingResult<EmployeeResponse> findEmployeesByAccountId(Long accountId, Pageable pageable, String query) {
        // Find the restaurant by account ID
        Restaurant restaurant = restaurantRepository.findByAccount_Id(accountId);
        if (restaurant == null) {
            throw new AppException(ErrorCode.NOT_EXIST);
        }

        // Fetch employees and return the paginated result
        return PagingResult.<EmployeeResponse>builder()
            .results(employeeRepository.findByRestaurant_IdAndEmployeeNameContaining(restaurant.getId(), query, pageable)
                .stream()
                .map(employeeMapper::toEmployeeResponse)
                .toList())
            .totalItems(employeeRepository.countByRestaurant_IdAndEmployeeNameContaining(restaurant.getId(), query))
            .build();
    }

    /**
     * Authenticates an employee using login credentials.
     *
     * @param request The login request containing credentials.
     * @return The authentication response with a token if successful.
     */
    @Override
    public AuthenticationResponse authenticated(EmployeeLoginRequest request) {
        // Find the account by phone number
        Account account = accountService.findAccountByPhoneNumber(request.getPhoneNumberOfRestaurant());

        // Find the employee by username and restaurant ID
        Employee employee = employeeRepository.findByUsernameAndRestaurant_Id(request.getUsername(), account.getRestaurant().getId())
            .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_EXIST));

        // Verify the password
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), employee.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        // Generate a JWT token for the authenticated employee
        String token = generateToken(employee);

        return AuthenticationResponse.builder()
            .token(token)
            .authenticated(true)
            .build();
    }

    /**
     * Updates an employee's password.
     *
     * @param employeeId The ID of the employee.
     * @param newPassword The new password to set.
     */
    @Override
    public void updateEmployeePassword(Long employeeId, String newPassword) {
        // Find the employee by ID
        Employee employee = findEmployeeById(employeeId);

        // Encode the new password and update it
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        employee.setPassword(passwordEncoder.encode(newPassword));

        // Save the updated employee
        employeeRepository.save(employee);
    }

    /**
     * Builds the scope string for an employee's permissions and role.
     *
     * @param employee The employee entity.
     * @return The scope string.
     */
    @Override
    public String buildScope(Employee employee) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (employee.getRole() != null) {
            stringJoiner.add("ROLE_" + employee.getRole().getName());
            if (employee.getRestaurant() != null) {
                employee.getRestaurant().getRestaurantPackage().getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            }
        }
        return stringJoiner.toString();
    }

    /**
     * Generates a JWT token for an employee.
     *
     * @param employee The employee entity.
     * @return The generated JWT token.
     */
    @Override
    public String generateToken(Employee employee) {
        // Build the JWT claims
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(employee.getEmployeeName())
            .issueTime(new Date())
            .expirationTime(new Date(Instant.now().plus(4, ChronoUnit.HOURS).toEpochMilli()))
            .claim("scope", buildScope(employee))
            .claim("restaurantId", employee.getRestaurant().getId())
            .claim("employeeId", employee.getId())
            .claim("accountId", employee.getRestaurant().getAccount().getId())
            .jwtID(UUID.randomUUID().toString())
            .build();

        // Create the JWT payload and header
        Payload payload = new Payload(claims.toJSONObject());
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWSObject token = new JWSObject(header, payload);

        // Sign the token using the signer key
        try {
            token.sign(new MACSigner(signerKey.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        // Return the serialized token
        return token.serialize();
    }
}