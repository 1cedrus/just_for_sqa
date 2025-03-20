package com.restaurent.manager.dto.request;

import com.restaurent.manager.custom.ValidEmail;
import com.restaurent.manager.custom.ValidPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest {
    @Valid
    @NotNull(message = "INVALID_USERNAME")
    @NotBlank(message = "INVALID_USERNAME")
    private String username;

    @NotBlank(message = "INVALID_PHONENUMBER")
    @NotNull(message = "INVALID_PHONENUMBER")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "INVALID_PHONENUMBER")
    private String phoneNumber;

    @Size(min = 8,message = "INVALID_PASSWORD")
    @NotNull(message = "NOT_NULL")
    @ValidPassword
    private String password;

    @NotNull(message = "INVALID_EMAIL")
    @ValidEmail(message = "INVALID_EMAIL")
    private String email;
}
