package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.AccountRequest;
import com.restaurent.manager.dto.request.AuthenticationRequest;
import com.restaurent.manager.dto.request.ForgotPasswordRequest;
import com.restaurent.manager.dto.request.VerifyAccount;
import com.restaurent.manager.dto.response.AccountResponse;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.VerifyResponse;
import com.restaurent.manager.entity.Account;
import org.springframework.data.domain.Pageable;


public interface IAccountService{
    AccountResponse register(AccountRequest req);
    PagingResult<AccountResponse> getAccountsManager(Pageable pageable, String query);
    VerifyResponse verifyAccount(VerifyAccount req);
    String regenerateOtp(String email);
    Account findAccountByID(Long id);
    AccountResponse getAccountById(Long id);
    Account findAccountByPhoneNumber(String phoneNumber);
    AuthenticationResponse authenticated(AuthenticationRequest req);
    AuthenticationResponse verifyOtp(VerifyAccount req);
    String sendOtp(String email);
    AuthenticationResponse authenticatedEmail(String email);
    void forgotPassword(ForgotPasswordRequest request);
    void changePassword(String newPassword ,AuthenticationRequest request);

}
