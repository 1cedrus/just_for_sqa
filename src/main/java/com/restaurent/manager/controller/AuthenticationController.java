package com.restaurent.manager.controller;

import com.nimbusds.jose.JOSEException;
import com.restaurent.manager.dto.request.IntrospectRequest;
import com.restaurent.manager.dto.request.employee.EmployeeLoginRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.IntrospectResponse;
import com.restaurent.manager.service.IAuthenticationService;
import com.restaurent.manager.service.IEmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/identify")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    IAuthenticationService authenticationService;
    IEmployeeService employeeService;
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username : " + authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
        return  ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid IntrospectRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PostMapping(value = "/employee/login")
    public ApiResponse<AuthenticationResponse> employeeLogin(@RequestBody EmployeeLoginRequest request){
        return ApiResponse.<AuthenticationResponse>builder()
                .result(employeeService.authenticated(request))
                .build();
    }
    @PostMapping(value = "/refresh-token/{token}")
    public ApiResponse<AuthenticationResponse> refreshToken(@PathVariable String token) throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(token))
                .build();
    }
}
