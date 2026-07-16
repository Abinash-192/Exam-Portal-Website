package com.examportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "options", indexes = {
        @Index(name = "idx_option_question", columnList = "question_id"),
        @Index(name = "idx_option_label",columnList = "option_label")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",nullable = false)
     private Question question;

    @Column(name = "option_label",nullable = false, length = 2)
     private String optionLabel;

    @Column(name = "option_text",nullable = false, columnDefinition = "TEXT")
     private String optionText;

    @Column(name = "option_code",columnDefinition = "TEXT")
     private String optionCode;
}
