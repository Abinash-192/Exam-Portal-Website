package com.examportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    @NotBlank(message = "Identifier (email/mobile) is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotBlank(message = "OTP type is required")
    private String type;
}
