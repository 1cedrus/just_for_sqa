package com.restaurent.manager.service.impl;


import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.restaurent.manager.dto.request.IntrospectRequest;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.IntrospectResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.InvalidTokenRepository;
import com.restaurent.manager.service.IAccountService;
import com.restaurent.manager.service.ITokenGenerate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AuthenticationServiceTest {

    AuthenticationService authenticationService;
    InvalidTokenRepository invalidTokenRepository;
    IAccountService accountService;
    ITokenGenerate tokenGenerate;

    Validator validator;
    String SIGNER_KEY = "12345678901234567890123456789012";

    @BeforeEach
    void setUp() {
        invalidTokenRepository = mock(InvalidTokenRepository.class);
        accountService = mock(IAccountService.class);
        tokenGenerate = mock(ITokenGenerate.class);
        authenticationService = spy(new AuthenticationService(invalidTokenRepository, accountService, tokenGenerate));
        authenticationService.SIGNER_KEY = SIGNER_KEY; // dummy 256-bit key

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testIntrospect_ValidToken() throws JOSEException, ParseException {
        // Given
        String token = "valid.token.here";
        IntrospectRequest request = new IntrospectRequest();
        request.setToken(token);

        doReturn(null).when(authenticationService).verifyToken(token);

        // When
        IntrospectResponse response = authenticationService.introspect(request);

        // Then
        assertTrue(response.isValid());
    }

    @Test
    void testIntrospect_InvalidToken_AppException() throws JOSEException, ParseException {
        // Given
        String token = "invalid.token.here";
        IntrospectRequest request = new IntrospectRequest();
        request.setToken(token);

        doThrow(new AppException(ErrorCode.UNAUTHENTICATED)).when(authenticationService).verifyToken(token);

        // When
        IntrospectResponse response = authenticationService.introspect(request);

        // Then
        assertFalse(response.isValid());
    }

    @Test
    void testIntrospectRequest_NullToken_ShouldFailValidation() {
        // Given
        IntrospectRequest request = new IntrospectRequest();
        request.setToken(null);

        // When
        Set<ConstraintViolation<IntrospectRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("token")));
    }

    @Test
    void testIntrospectRequest_BlankToken_ShouldFailValidation() {
        // Given
        IntrospectRequest request = new IntrospectRequest();
        request.setToken("   ");

        // When
        Set<ConstraintViolation<IntrospectRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("token")));
    }

    private String generateToken(Date expiration, boolean signCorrectly, String jwtId) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .expirationTime(expiration)
                .jwtID(jwtId)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        if (signCorrectly) {
            signedJWT.sign(new MACSigner(SIGNER_KEY.getBytes()));
        } else {
            signedJWT.sign(new MACSigner("wrong-signing-key-00000000000000".getBytes()));
        }

        return signedJWT.serialize();
    }

    @Test
    void testVerifyToken_validToken() throws Exception {
        // Given
        String jwtId = UUID.randomUUID().toString();
        String token = generateToken(new Date(System.currentTimeMillis() + 60000), true, jwtId); // valid 1 min
        when(invalidTokenRepository.existsById(jwtId)).thenReturn(false);

        // When
        SignedJWT result = authenticationService.verifyToken(token);

        // Then
        assertNotNull(result);
        assertEquals(jwtId, result.getJWTClaimsSet().getJWTID());
    }

    @Test
    void testVerifyToken_invalidSignature_shouldThrowAppException() throws Exception {
        // Given
        String token = generateToken(new Date(System.currentTimeMillis() + 60000), false, UUID.randomUUID().toString());

        // When / Then
        assertThrows(AppException.class, () -> authenticationService.verifyToken(token));
    }

    @Test
    void testVerifyToken_expired_shouldThrowAppException() throws Exception {
        // Given
        String token = generateToken(new Date(System.currentTimeMillis() - 1000), true, UUID.randomUUID().toString()); // expired

        // When / Then
        assertThrows(AppException.class, () -> authenticationService.verifyToken(token));
    }

    @Test
    void testVerifyToken_revoked_shouldThrowAppException() throws Exception {
        // Given
        String jwtId = UUID.randomUUID().toString();
        String token = generateToken(new Date(System.currentTimeMillis() + 60000), true, jwtId);
        when(invalidTokenRepository.existsById(jwtId)).thenReturn(true); // revoked

        // When / Then
        assertThrows(AppException.class, () -> authenticationService.verifyToken(token));
    }

    private String createSignedToken(Date expiration, String jwtId, Object accountId) throws JOSEException {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .expirationTime(expiration)
                .jwtID(jwtId);

        if (accountId != null) {
            builder.claim("accountId", accountId);
        }

        JWTClaimsSet claimsSet = builder.build();

        SignedJWT signedJWT = new SignedJWT(
                new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(new MACSigner(SIGNER_KEY.getBytes()));
        return signedJWT.serialize();
    }

    @Test
    void testRefreshToken_validTokenWithAccountId_shouldReturnNewToken() throws Exception {
        String jwtId = UUID.randomUUID().toString();
        Long accountIdInt = 123L;
        Long accountId = accountIdInt.longValue();

        // Tạo JWT với accountId claim
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(jwtId)
                .expirationTime(new Date(System.currentTimeMillis() + 60000))
                .claim("accountId", accountIdInt)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );
        signedJWT.sign(new MACSigner("12345678901234567890123456789012")); // SIGNER_KEY >= 32 ký tự

        // Tạo instance thật và inject mock
        AuthenticationService authService = new AuthenticationService(
                invalidTokenRepository,
                accountService,
                tokenGenerate
        );
        ReflectionTestUtils.setField(authService, "SIGNER_KEY", "12345678901234567890123456789012");

        // Spy và mock phương thức verifyToken
        AuthenticationService spyAuth = Mockito.spy(authService);
        doReturn(signedJWT).when(spyAuth).verifyToken(anyString());

        // Mock accountService và tokenGenerate
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setUsername("test_user");

        when(accountService.findAccountByID(accountId)).thenReturn(mockAccount);
        when(tokenGenerate.generateToken(mockAccount)).thenReturn("new.token.here");

        // Act
        AuthenticationResponse response = spyAuth.refreshToken("dummy-token");

        // Assert
        assertTrue(response.isAuthenticated());
        assertEquals("new.token.here", response.getToken());
    }

    @Test
    void testRefreshToken_validTokenButMissingAccountId_shouldReturnUnauthenticated() throws Exception {
        // Given
        String jwtId = UUID.randomUUID().toString();
        String token = createSignedToken(new Date(System.currentTimeMillis() + 60000), jwtId, null);

        SignedJWT signedJWT = SignedJWT.parse(token);
        doReturn(signedJWT).when(authenticationService).verifyToken(token);

        // When
        AuthenticationResponse response = authenticationService.refreshToken(token);

        // Then
        assertFalse(response.isAuthenticated());
        assertNull(response.getToken());
    }

    @Test
    void testRefreshToken_invalidToken_shouldThrowAppException() throws Exception {
        // Given
        String token = "invalid.token.string";
        doThrow(new AppException(ErrorCode.UNAUTHENTICATED)).when(authenticationService).verifyToken(token);

        // Then
        assertThrows(AppException.class, () -> authenticationService.refreshToken(token));
    }
}

