package com.restaurent.manager.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.restaurent.manager.custom.PasswordGenerator;
import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.AccountRequest;
import com.restaurent.manager.dto.request.AuthenticationRequest;
import com.restaurent.manager.dto.request.ForgotPasswordRequest;
import com.restaurent.manager.dto.request.VerifyAccount;
import com.restaurent.manager.dto.response.AccountResponse;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.VerifyResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.enums.EmailContainer;
import com.restaurent.manager.enums.RoleSystem;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.AccountMapper;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.service.IAccountService;
import com.restaurent.manager.service.IEmailService;
import com.restaurent.manager.service.IRoleService;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AccountService implements IAccountService, ITokenGenerate<Account> {
    @NonFinal
    @Value("${jwt.signerKey}")
    private String signerKey;

    AccountRepository accountRepository;
    AccountMapper accountMapper;
    IEmailService emailService;
    IRoleService roleService;

    @Override
    public AccountResponse register(AccountRequest req) {
        if(accountRepository.existsByEmailAndStatus(req.getEmail(), true)){
            throw new AppException(ErrorCode.EMAIL_EXIST);
        }
        if(accountRepository.existsByPhoneNumberAndStatus(req.getPhoneNumber(), true)){
            throw new AppException(ErrorCode.PHONENUMBER_EXIST);
        }
        if(accountRepository.existsByEmailAndStatus(req.getEmail(),false)){
            accountRepository.delete(accountRepository.findByEmail(req.getEmail()).orElseThrow());
        }
        Account account = accountMapper.toAccount(req);
        String otp = emailService.generateCode(6);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setStatus(false);
        account.setOtp(otp);
        account.setOtpGeneratedTime(LocalDateTime.now());
        Role role = roleService.findByRoleName(RoleSystem.MANAGER.name());
        role.assignAccount(account);
        emailService.sendEmail(account.getEmail(),EmailContainer.formMailBody(otp),"Verify account ");
        Account saved = accountRepository.save(account);
        return accountMapper.toAccountResponse(saved);
    }

    @Override
    public PagingResult<AccountResponse> getAccountsManager(Pageable pageable, String query) {
        return PagingResult.<AccountResponse>builder()
                .results(accountRepository.findByRole_IdAndUsernameContaining(5L,query,pageable).stream().map(accountMapper::toAccountResponse).toList())
                .totalItems(accountRepository.countByRole_IdAndUsernameContaining(5L,query))
                .build();
    }


    @Override
    public VerifyResponse verifyAccount(VerifyAccount req) {
        Account account = accountRepository.findByEmail(req.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(account.isStatus()){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if(account.getOtp().equals(req.getOtp()) && Duration.between(LocalDateTime.now(), account.getOtpGeneratedTime()).getSeconds() < 60){
            account.setStatus(true);
            return VerifyResponse.builder()
                    .status(accountRepository.save(account).isStatus())
                    .email(account.getEmail())
                    .build();
        }
        return VerifyResponse.builder()
                .email(account.getEmail())
                .status(false)
                .build();
    }

    @Override
    public AuthenticationResponse authenticated(AuthenticationRequest req) {
        Account account = accountRepository.findByEmailAndStatus(req.getEmail(),true).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated =  passwordEncoder.matches(req.getPassword(), account.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        String token = generateToken(account);
        accountRepository.save(account);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }

    @Override
    public AuthenticationResponse verifyOtp(VerifyAccount req) {
        Account account = accountRepository.findByEmail(req.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        if(req.getOtp().equals(account.getOtp())){
            return AuthenticationResponse.builder()
                    .authenticated(true)
                    .token(generateToken(account))
                    .build();
        }
        return AuthenticationResponse.builder()
                .authenticated(false)
                .build();
    }

    @Override
    public String sendOtp(String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.EMAIL_NOT_EXIST)
        );
        String otp = emailService.generateCode(6);
        account.setOtp(otp);
        accountRepository.save(account);
        emailService.sendEmail(account.getEmail(), EmailContainer.formMailBody(otp),"Verify Account");
        return otp;
    }

    @Override
    public AuthenticationResponse authenticatedEmail(String email) {
        return AuthenticationResponse.builder()
                .authenticated(true)
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findByEmailAndPhoneNumber(request.getEmail(),request.getPhoneNumber()).orElseThrow(
                () -> new AppException(ErrorCode.ACCOUNT_NOT_EXIST)
        );
        String password = PasswordGenerator.generateRandomPassword(6);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        account.setPassword(passwordEncoder.encode(password));
        emailService.sendEmail(account.getEmail(),EmailContainer.formMailForgot(password), "Reset password");
        accountRepository.save(account);
    }

    @Override
    public void changePassword(String newPassword, AuthenticationRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated =  passwordEncoder.matches(request.getPassword(), account.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Override
    public String regenerateOtp(String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String otp = emailService.generateCode(6);
        account.setOtp(otp);
        account.setOtpGeneratedTime(LocalDateTime.now());
        accountRepository.save(account);
        emailService.sendEmail(account.getEmail(),EmailContainer.formMailBody(otp),"Verify account ");
        return "New otp are generated !";
    }

    @Override
    public Account findAccountByID(Long id) {
        return accountRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
    }

    @Override
    public AccountResponse getAccountById(Long accountId) {
        return accountMapper.toAccountResponse(
                findAccountByID(accountId)
        );
    }

    @Override
    public Account findAccountByPhoneNumber(String phoneNumber) {
        return accountRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }
    @Override
    public String generateToken(Account user){
        String restaurantId = "";
        String packName = "";
        if(user.getRestaurant() != null && !user.getRole().getName().equals(RoleSystem.ADMIN.name())){
            restaurantId = user.getRestaurant().getId().toString();
            packName = user.getRestaurant().getRestaurantPackage().getPackName();
        }
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(user.getUsername())
                .subject(user.getUsername())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(4, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope",buildScope(user))
                .claim("email",user.getEmail())
                .claim("restaurantId",restaurantId)
                .claim("accountId",user.getId())
                .claim("packName", packName)
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
    @Override
    public String buildScope(Account user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(user.getRole() != null){
            stringJoiner.add("ROLE_" + user.getRole().getName());
            if(user.getRestaurant() != null){
                user.getRestaurant().getRestaurantPackage().getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            }
        }
        return stringJoiner.toString();
    }




}
