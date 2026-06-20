package com.examportal.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2 , max = 30, message = "name must be 2- 30 characters")
    private String name;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String mobile;

    @Size(max = 300 , message = "Bio must not exceed 300 characters")
    private String bio;

    private String profilePicture;
}
