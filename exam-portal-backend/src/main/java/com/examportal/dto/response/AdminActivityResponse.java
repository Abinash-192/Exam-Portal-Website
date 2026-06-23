package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminActivityResponse {

    private Long id;
    private String adminName;
    private String adminEmail;
    private String actionType;
    private String targetUserName;
    private String targetUserEmail;
    private String description;
    private LocalDateTime performedAt;
}
