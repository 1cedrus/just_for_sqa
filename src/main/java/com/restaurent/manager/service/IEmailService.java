package com.restaurent.manager.service;

public interface IEmailService {
    void sendEmail(String email,String body, String subject);
    String generateCode(int length);
}
