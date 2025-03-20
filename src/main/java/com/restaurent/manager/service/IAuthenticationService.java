package com.restaurent.manager.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.restaurent.manager.dto.request.IntrospectRequest;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface IAuthenticationService {
    IntrospectResponse introspect(IntrospectRequest req) throws JOSEException, ParseException;

    void logout(IntrospectRequest request) throws ParseException, JOSEException;

    SignedJWT verifyToken(String token) throws JOSEException, ParseException;

    AuthenticationResponse refreshToken(String token) throws ParseException, JOSEException;
}
