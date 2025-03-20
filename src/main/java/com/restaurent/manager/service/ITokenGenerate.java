package com.restaurent.manager.service;

public interface ITokenGenerate<T> {
    String buildScope(T request);
    String generateToken(T request);
}
