package com.restaurent.manager.controller;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.AccountRequest;
import com.restaurent.manager.dto.request.AuthenticationRequest;
import com.restaurent.manager.dto.request.ForgotPasswordRequest;
import com.restaurent.manager.dto.request.VerifyAccount;
import com.restaurent.manager.dto.response.*;
import com.restaurent.manager.service.IAccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@AllArgsConstructor
@Slf4j
public class AccountController {
    IAccountService accountService;
    @PostMapping("/register")
    public ApiResponse<AccountResponse> register(@RequestBody @Valid AccountRequest req){
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.register(req))
                .build();
    }
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @GetMapping(value = "/manager")
    public ApiResponse<PagingResult<AccountResponse>> getAccountsByRoleId(@RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size,
                                                         @RequestParam(value = "query",defaultValue = "") String query){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<PagingResult<AccountResponse>>
                builder()
                .result(accountService.getAccountsManager(pageable,query))
                .build();
    }
    @PostMapping("/verify")
    public ApiResponse<VerifyResponse> verifyAccount(@RequestBody @Valid VerifyAccount req){
        VerifyResponse verifyResponse = accountService.verifyAccount(req);
        if(verifyResponse.isStatus()){
           return ApiResponse.<VerifyResponse>builder()
                    .result(verifyResponse)
                    .message("Verify success")
                    .build();
        }
        return  ApiResponse.<VerifyResponse>builder()
                .result(verifyResponse)
                .message("Verify failed")
                .build();
    }
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest req){
        return ApiResponse.<AuthenticationResponse>builder()
                .result(accountService.authenticated(req))
                .build();
    }
    @PostMapping("/regenerated-otp/{email}")
    public ApiResponse<String> regeneratedOtp(@PathVariable String email){
        return ApiResponse.<String>builder()
                .result(accountService.regenerateOtp(email))
                .build();
    }
    @GetMapping("/{accountId}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable Long accountId){
        return  ApiResponse.<AccountResponse>builder()
                .result(accountService.getAccountById(accountId))
                .build();
    }
    @PostMapping(value = "/verify/otp")
    public ApiResponse<AuthenticationResponse> verifyOtp(@RequestBody VerifyAccount req){
        return ApiResponse.<AuthenticationResponse>builder()
                .result(accountService.verifyOtp(req))
                .build();
    }
    @PostMapping(value = "/{email}/login")
    public ApiResponse<AuthenticationResponse> loginByEmail(@PathVariable String email){
        return ApiResponse.<AuthenticationResponse>builder()
                .result(accountService.authenticatedEmail(email))
                .build();
    }
    @PostMapping(value = "/{email}/send-otp")
    public ApiResponse<String> sendOtpLogin(@PathVariable String email){
        return ApiResponse.<String>builder()
                .result(accountService.sendOtp(email))
                .build();
    }
    @PostMapping(value = "/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request){
        accountService.forgotPassword(request);
        return ApiResponse.<Void>builder()
                .message("success")
                .build();
    }
    @PostMapping(value = "/change-password/{newPassword}")
    public ApiResponse<Void> changePassword(@RequestBody AuthenticationRequest request,@PathVariable String newPassword){
        accountService.changePassword(newPassword, request);
        return  ApiResponse.<Void>builder()
                .message("success")
                .build();
    }
}
