package com.examportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification", indexes = {
        @Index(name = "idx_otp_verification", columnList = "identifier"),
        @Index(name = "idx_otp_type",   columnList = "type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(nullable = false)
     private String identifier;

     @Column(name = "otp_code", nullable = false , length = 6)
     private String otpCode;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private OtpType type;

     @Column(name = "expires_at", nullable = false)
     private LocalDateTime expiresAt;

     @Column(name = "is_used")
     private boolean used ;

     @Column(name = "attempt_count")
     private int attemptCount = 0 ;

     @Column(name = "created_at", updatable = false)
     private LocalDateTime createdAt;

     @PrePersist
     protected  void  onCreate(){
         createdAt = LocalDateTime.now();
     }

     public enum OtpType{ EMAIL, MOBILE,PASSWORD_RESET}

}
