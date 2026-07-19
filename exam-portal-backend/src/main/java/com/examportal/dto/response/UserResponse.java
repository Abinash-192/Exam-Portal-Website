////package com.examportal.dto.response;
////
////import com.fasterxml.jackson.annotation.JsonInclude;
////import lombok.Builder;
////import lombok.Data;
////
////import java.time.LocalDateTime;
////
////
////@Data
////@Builder
////@JsonInclude(JsonInclude.Include.NON_NULL)
////public class UserResponse {
////
////    private  Long id;
////    private String name;
////    private String email;
////    private String mobile;
////    private String role;
////    private String profilePicture;
////    private String provider;
////
////    private boolean approved;
////    private boolean blocked;
////    private boolean enabled;
////    private boolean emailVerified;
////    private boolean mobileVerified;
////
////    private LocalDateTime createdAt;
////    private LocalDateTime updatedAt;
////
////    private Integer totalExamsTaken;
////    private Integer totalExamsPassed;
////}
//
//package com.examportal.dto.response;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.Builder;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//
//@Data
//@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class UserResponse {
//
//    // ── Identity ──────────────────────────────────────────────────
//    private Long   id;
//    private String name;
//    private String email;
//    private String mobile;
//    private String role;
//    private String provider;
//    private String profilePicture;
//    private String bio;
//
//    // ── Status ────────────────────────────────────────────────────
//    private boolean approved;
//    private boolean blocked;
//    private boolean enabled;
//    private boolean emailVerified;
//    private boolean mobileVerified;
//
//    // ── Timestamps ────────────────────────────────────────────────
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private LocalDateTime lastLoginAt;
//
//    // ── Exam summary (optional — populated for dashboard) ─────────
//    private Integer totalExamsTaken;
//    private Integer totalExamsPassed;
//    private Integer totalExamsFailed;
//    private Double  averageScore;
//    private Double  averagePercentage;
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
public class UserResponse {

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
    private boolean approved;
    private boolean blocked;
    private boolean enabled;
    private boolean emailVerified;
    private boolean mobileVerified;

    // ── Timestamps ────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // ── Exam stats ────────────────────────────────────────────────
    private Integer totalExamsTaken;
    private Integer totalExamsPassed;
    private Integer totalExamsFailed;
    private Double  averageScore;
    private Double  averagePercentage;
}