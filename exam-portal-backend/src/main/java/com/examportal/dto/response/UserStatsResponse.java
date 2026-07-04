package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStatsResponse {

    private long userId;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String provider;
    private String profilePicture;

    private boolean approved;
    private boolean blocked;
    private boolean emailVerified;

    private int totalExamsTaken;
    private int totalExamsPassed;
    private int totalExamsFailed;
    private double averageScore;
    private double averagePercentage;
    private String bestPerformanceBand;

    private LocalDateTime joinedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastExamDate;

    private List<AttemptResultResponse> examHistory;
}
