package com.examportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_question_exam",columnList = "exam_id"),
        @Index(name = "idx_question_order",columnList = "question_order"),
        @Index(name = "idx_question_language",columnList = "language"),
        @Index(name = "idx_question_type",columnList = "question_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(name = "content",
             nullable = false,
             columnDefinition = "TEXT")
     private String content;

     @Column(name = "code_snippet",
             columnDefinition = "TEXT")
     private String codeSnippet;

     @Column(name = "language",length = 30)
     private String language;

    @Column(name = "question_type", length = 20)
     private String questionType;

    @Column(name = "question_order",nullable = false)
    private int questionOrder;

     @Column(name = "marks",nullable = false)
     @Builder.Default
     private int marks = 1;

     @Column(name = "explanation",
             columnDefinition = "TEXT")
     private String explanation;

     @Column(name = "correct_option_id")
     private Long correctOptionId;

     @Column(name = "negative_marks")
     @Builder.Default
     private double negativeMarks = 0.25;

     @Column(name = "is_ai_generated")
     @Builder.Default
     private boolean aiGenerated = false;

     @JoinColumn(name = "exam_id",nullable = false)
     @ManyToOne(fetch = FetchType.LAZY)
     private Exam exam;

     @OneToMany(mappedBy = "question",
             cascade = CascadeType.ALL,
             orphanRemoval = true,
             fetch = FetchType.EAGER)
     @OrderBy("optionLabel ASC")
     @Builder.Default
     private List<Option>  options = new ArrayList<>();
}
