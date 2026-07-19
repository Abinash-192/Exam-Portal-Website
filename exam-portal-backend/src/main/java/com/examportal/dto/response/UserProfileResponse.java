//package com.examportal.dto.response;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.Builder;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//
//@Data
//@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class UserProfileResponse {
//
//    private Long id;
//    private String name;
//    private String email;
//    private String role;
//    private String provider;
//    private String profilePicture;
//    private String bio;
//
//
//    private boolean emailVerified;
//    private boolean mobileVerified;
//    private boolean approved;
//    private boolean blocked;
//
//    private LocalDateTime createdAt;
//    private LocalDateTime lastLoginAt;
//
//    private int totalExamsTaken;
//    private int totalExamsPassed;
//    private double averagePercentage;
//    private String performanceBand;
//}
//


package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    // ── Identity ──────────────────────────────────────────────────
    private Long   id;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String provider;
    private String profilePicture;
    private String bio;

    // ── Status ────────────────────────────────────────────────────
    private boolean emailVerified;
    private boolean mobileVerified;
    private boolean approved;
    private boolean blocked;

    // ── Timestamps ────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // ── Exam stats ────────────────────────────────────────────────
    private int    totalExamsTaken;
    private int    totalExamsPassed;
    private double averagePercentage;
    private String performanceBand;
}