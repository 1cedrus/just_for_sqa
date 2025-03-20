package com.restaurent.manager.dto.request;

import com.restaurent.manager.custom.ValidEmail;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyAccount {
    @ValidEmail(message = "INVALID_EMAIL")
    private String email;
    private String otp;
}
