package com.restaurent.manager.custom.impl;

import com.restaurent.manager.custom.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String DOMAIN_PATTERN = "^[A-Za-z0-9+_.-]+@(gmail\\.com|yahoo\\.com|hotmail\\.com|fpt\\.edu\\.vn)$";

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return false;
        }
        return Pattern.matches(EMAIL_PATTERN, email) && Pattern.matches(DOMAIN_PATTERN, email);
    }
}