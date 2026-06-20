//package com.examportal.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "users", indexes = {
//        @Index(name = "idx_user_email",columnList = "email",unique = true),
//        @Index(name = "idx_user_mobile", columnList = "mobile", unique = true)
//})
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class User {
//
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 100)
//    private String name;
//
//    @Column(unique = true, nullable = false, length = 150)
//    private String email;
//
//    @Column(unique = true, length = 15)
//    private String mobile;
//
//    private String password;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Role role;
//
//    @Column(name = "is_enabled", nullable = false)
//    private boolean enabled = false;
//
//    @Column(name = "is_approved", nullable = false)
//    private boolean approved = false;
//
//    @Column(name = "is_blocked", nullable = false)
//    private boolean blocked = false;
//
//    @Column(name = "email_verified", nullable = false)
//    private boolean emailVerified = false;
//
//    @Column(name = "mobile_verified", nullable = false)
//    private boolean mobileVerified = false;
//
//    @Column(length = 30)
//    private String provider;
//
//    @Column(name = "profile_picture")
//    private String profilePicture;
//
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @OneToMany(mappedBy = "user",
//            cascade = CascadeType.ALL,
//            fetch = FetchType.LAZY)
//    private List<ExamAttempt> examAttempts;
//}

package com.examportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_email",
                        columnList = "email",  unique = true),
                @Index(name = "idx_user_mobile",
                        columnList = "mobile", unique = true),
                @Index(name = "idx_user_role",
                        columnList = "role")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Personal info ─────────────────────────────────────────────
    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(unique = true, length = 15)
    private String mobile;

    // ── Credentials ───────────────────────────────────────────────
    // null for OAuth2-only accounts (Google / GitHub)
    @Column(length = 255)
    private String password;

    // ── Role ─────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
//    @Builder.Default
    private Role role ;

    // ── Status flags ──────────────────────────────────────────────
    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean approved = false;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private boolean blocked = false;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "mobile_verified", nullable = false)
    @Builder.Default
    private boolean mobileVerified = false;

    // ── OAuth2 provider (local | google | github) ──────────────────
    @Column(name = "provider", length = 30)
    @Builder.Default
    private String provider = "local";

    // ── Profile ───────────────────────────────────────────────────
    @Column(name = "profile_picture", length = 500)
    private String profilePicture;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // ── Timestamps ────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // ── Relations ─────────────────────────────────────────────────
    @OneToMany(mappedBy = "user",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    private List<ExamAttempt> examAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "user",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();
}