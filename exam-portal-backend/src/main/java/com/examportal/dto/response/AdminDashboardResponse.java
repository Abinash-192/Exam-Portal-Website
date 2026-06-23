package com.examportal.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalAdmins;
    private long pendingApprovals;
    private long blockedUsers;
    private long approvedUsers;

    private long totalActiveExams;
    private long totalExams;

    private long totalAttempts;
    private double overallPassRate;

    private Map<String, Long> registrationsLast7Days;

    private List<AdminActivityResponse> recentActivity;
}
