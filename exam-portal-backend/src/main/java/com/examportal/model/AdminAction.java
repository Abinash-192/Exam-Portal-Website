//package com.examportal.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "admin_actions", indexes = {
//        @Index(name = "idx_action_admin", columnList = "admin_id"),
//        @Index(name = "idx_action_target", columnList = "target_user_id"),
//        @Index(name = "idx_action_type", columnList = "action_type")
//})
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class AdminAction {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "admin_id", nullable = false)
//    private User admin;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "target_user_id")
//    private User targetUser;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "action_type", nullable = false)
//    private ActionType actionType;
//
//    @Column(name = "description", columnDefinition = "TEXT")
//    private String description;
//
//    @CreationTimestamp
//    @Column(name = "performed_at", updatable = false)
//    private LocalDateTime performedAt;
//
//    public  enum ActionType{
//        APPROVE_USER,
//        BLOCK_USER,
//        UNBLOCK_USER,
//        DELETE_USER,
//        CREATE_EXAM,
//        UPDATE_EXAM,
//        DELETE_EXAM,
//        TOGGLE_EXAM,
//        ADD_QUESTION,
//        DELETE_QUESTION,
//        RESEND_RESULT_EMAIL
//
//    }
//}


package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_actions",
        indexes = {
                @Index(name = "idx_action_admin",
                        columnList = "admin_id"),
                @Index(name = "idx_action_target",
                        columnList = "target_user_id"),
                @Index(name = "idx_action_type",
                        columnList = "action_type"),
                @Index(name = "idx_action_performed_at",
                        columnList = "performed_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Admin who performed the action ────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    // ── Target user (nullable — exam actions have no target) ──────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    // ── Action type ───────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;

    // ── Optional description / reason ─────────────────────────────
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ── Timestamp ─────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "performed_at", updatable = false)
    private LocalDateTime performedAt;

    // ── Action types ──────────────────────────────────────────────
    public enum ActionType {
        // User actions
        APPROVE_USER,
        BLOCK_USER,
        UNBLOCK_USER,
        DELETE_USER,
        UPDATE_USER,

        // Exam actions
        CREATE_EXAM,
        UPDATE_EXAM,
        DELETE_EXAM,
        TOGGLE_EXAM,

        // Question actions
        ADD_QUESTION,
        UPDATE_QUESTION,
        DELETE_QUESTION,
        BULK_ADD_QUESTIONS,
        REORDER_QUESTIONS,

        // Result actions
        RESEND_RESULT_EMAIL
    }
}