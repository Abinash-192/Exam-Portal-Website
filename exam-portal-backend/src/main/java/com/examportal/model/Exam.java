package com.examportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams", indexes = {
        @Index(name = "idx_exam_category", columnList = "category"),
        @Index(name = "idx_exam_active", columnList = "is_active"),
        @Index(name = "idx_exam_difficulty", columnList = "difficulty"),
        @Index(name = "idx_exam_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false,length = 50)
    private String category;

    @Column(name = "duration_minutes",nullable = false)
    private int durationMintues;

    @Column(name = "passing_marks",nullable = false)
    private int totalMarks;

    @Column(name = "passing_marks",nullable = false)
    private int passingMarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty",nullable = false,length = 10)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    @Column(name = "is_active",nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "thumbnail_url",length = 500)
    private String thumbnailUrl;

    @Column(name = "tags",length = 300)
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy =  "exam",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("questionOrder ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "exam",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExamAttempt> attempts = new ArrayList<>();

    public enum DifficultyLevel{
        EASY,MEDIUM,HARD
    }
}
