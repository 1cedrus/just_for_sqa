package com.restaurent.manager.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.restaurent.manager.dto.request.AuthenticationRequest;
import com.restaurent.manager.dto.request.IntrospectRequest;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.IntrospectResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.InvalidToken;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.InvalidTokenRepository;
import com.restaurent.manager.service.IAccountService;
import com.restaurent.manager.service.IAuthenticationService;
import com.restaurent.manager.service.IEmployeeService;
import com.restaurent.manager.service.ITokenGenerate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService implements IAuthenticationService {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;
    InvalidTokenRepository invalidTokenRepository;
    IEmployeeService employeeService;
    IAccountService accountService;
    ITokenGenerate<Account> tokenGenerate ;

    @Override
    public IntrospectResponse introspect(IntrospectRequest req) throws JOSEException, ParseException {
        boolean isValid = true;
        try {
        verifyToken(req.getToken());
        }catch (AppException e ){
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    @Override
    public void logout(IntrospectRequest request) throws ParseException, JOSEException {
      try {
          var signJWT = verifyToken(request.getToken());
          InvalidToken invalidToken = InvalidToken.builder()
                  .id(signJWT.getJWTClaimsSet().getJWTID())
                  .expireDate(signJWT.getJWTClaimsSet().getExpirationTime())
                  .build();
          invalidTokenRepository.save(invalidToken);
      } catch (AppException e){
        log.info(e.getMessage());
      }
    }

    @Override
    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expireTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if(!verified || expireTime.before(new Date())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if(invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    @Override
    public AuthenticationResponse refreshToken(String token) throws ParseException, JOSEException {
        var signJWT = verifyToken(token);

        InvalidToken invalidToken = InvalidToken.builder()
                .id(signJWT.getJWTClaimsSet().getJWTID())
                .expireDate(signJWT.getJWTClaimsSet().getExpirationTime())
                .build();

        invalidTokenRepository.save(invalidToken);

        var accountId = signJWT.getJWTClaimsSet().getClaim("accountId");
        if(accountId != null){
            Account account = accountService.findAccountByID((Long) accountId);
            return AuthenticationResponse.builder()
                    .token(tokenGenerate.generateToken(account))
                    .build();
        }
        return AuthenticationResponse.builder()
                .authenticated(false)
                .build();
    }
}
