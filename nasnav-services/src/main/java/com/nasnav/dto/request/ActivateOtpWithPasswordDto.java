package com.nasnav.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivateOtpWithPasswordDto {
    @NotNull
    @Email
    private String email;

    @NotEmpty
    @NotNull
    private String otp;

    @NotNull
    private Long orgId;

    @NotNull
    private String password;
}
