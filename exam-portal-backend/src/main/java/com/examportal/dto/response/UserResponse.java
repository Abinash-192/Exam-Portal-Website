package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private  Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String profilePicture;
    private String provider;

    private boolean approved;
    private boolean blocked;
    private boolean enabled;
    private boolean emailVerified;
    private boolean mobileVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer totalExamsTaken;
    private Integer totalExamsPassed;
}
