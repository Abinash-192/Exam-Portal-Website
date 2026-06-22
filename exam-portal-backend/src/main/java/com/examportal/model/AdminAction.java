package com.examportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_actions", indexes = {
        @Index(name = "idx_action_admin", columnList = "admin_id"),
        @Index(name = "idx_action_target", columnList = "target_user_id"),
        @Index(name = "idx_action_type", columnList = "action_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "performed_at", updatable = false)
    private LocalDateTime performedAt;

    public  enum ActionType{
        APPROVE_USER,
        BLOCK_USER,
        UNBLOCK_USER,
        DELETE_USER,
        CREATE_EXAM,
        UPDATE_EXAM,
        DELETE_EXAM,
        TOGGLE_EXAM,
        ADD_QUESTION,
        DELETE_QUESTION,
        RESEND_RESULT_EMAIL

    }
}
