//package com.examportal.dto.response;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.Builder;
//import lombok.Data;
//
//import java.util.List;
//
//@Data
//@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class UserDashboardResponse {
//
//    // ── Profile snapshot ──────────────────────────────────────────
//    private UserResponse profile;
//
//    // ── Exam stats ────────────────────────────────────────────────
//    private int    totalExamsTaken;
//    private int    totalExamsPassed;
//    private int    totalExamsFailed;
//    private double averagePercentage;
//    private double averageScore;
//    private String performanceBand;
//
//    // ── Available exams count ─────────────────────────────────────
//    private long   availableExams;
//
//    // ── Recent 5 results ──────────────────────────────────────────
//    private List<AttemptResultResponse> recentResults;
//
//    // ── Unread notification count ─────────────────────────────────
//    private long   unreadNotifications;
//}


package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDashboardResponse {

    // ── Profile snapshot ──────────────────────────────────────────
    private UserResponse profile;

    // ── Exam stats ────────────────────────────────────────────────
    private int    totalExamsTaken;
    private int    totalExamsPassed;
    private int    totalExamsFailed;
    private double averageScore;
    private double averagePercentage;
    private String performanceBand;

    // ── Available exams ───────────────────────────────────────────
    private long availableExams;

    // ── Recent 5 results ──────────────────────────────────────────
    private List<AttemptResultResponse> recentResults;

    // ── Notifications ─────────────────────────────────────────────
    private long unreadNotifications;
}