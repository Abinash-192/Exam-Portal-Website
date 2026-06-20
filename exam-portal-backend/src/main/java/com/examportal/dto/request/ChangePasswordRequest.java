package com.examportal.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 10, message = "New password must be 8 characters.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$" , message = "New password must contain uppercase ,"+ "lowercase, & a digit")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
