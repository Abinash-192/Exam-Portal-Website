package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimerStatusResponse {

    private Long   attemptId;
    private String status;

    // ── Timer ─────────────────────────────────────────────────────
    private int    allowedTimeSeconds;
    private int    elapsedSeconds;
    private int    remainingSeconds;
    private double progressPercentage;

    // ── Flags ─────────────────────────────────────────────────────
    private boolean isExpired;
    private boolean isWarning;    // < 5 min left
    private boolean isCritical;   // < 1 min left
    private boolean autoSubmitted;

    // ── Timestamps ────────────────────────────────────────────────
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private String        serverTime;
}