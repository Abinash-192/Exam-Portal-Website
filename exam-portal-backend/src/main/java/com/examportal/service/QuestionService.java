package com.examportal.service;

import com.examportal.dto.request.BulkQuestionRequest;
import com.examportal.dto.request.QuestionFilterRequest;
import com.examportal.dto.request.QuestionRequest;
import com.examportal.dto.request.ReorderQuestionsRequest;
import com.examportal.dto.response.QuestionResponse;
import com.examportal.dto.response.QuestionStatsResponse;
import com.examportal.dto.response.QuestionSummaryResponse;
import com.examportal.exception.ResourceNotFoundException;
import com.examportal.exception.ValidationException;
import com.examportal.model.AdminAction;
import com.examportal.model.Exam;
import com.examportal.model.Option;
import com.examportal.model.Question;
import com.examportal.repository.AdminActionRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.OptionRepository;
import com.examportal.repository.QuestionRepository;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository    questionRepository;
    private final OptionRepository      optionRepository;
    private final ExamRepository        examRepository;
    private final UserRepository        userRepository;
    private final AdminActionRepository actionRepository;

    // ═════════════════════════════════════════════════════════════
    // ADD SINGLE QUESTION
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public QuestionResponse addQuestion(Long examId,
                                        QuestionRequest req) {

        Exam exam = findExamOrThrow(examId);

        validateOptionLabels(req);

        // Auto-assign order if not provided
        int order = req.getQuestionOrder() > 0
                ? req.getQuestionOrder()
                : questionRepository
                .findMaxOrderByExamId(examId) + 1;

        Question question = Question.builder()
                .exam(exam)
                .content(req.getContent().trim())
                .codeSnippet(req.getCodeSnippet())
                .language(normalizeLanguage(req.getLanguage()))
                .questionType(normalizeType(req.getQuestionType()))
                .marks(req.getMarks() > 0 ? req.getMarks() : 1)
                .negativeMarks(req.getNegativeMarks())
                .questionOrder(order)
                .explanation(req.getExplaination())
                .aiGenerated(req.isAiGenerated())
                .build();

        List<Option> options =
                buildOptions(req.getOptions(), question);
        question.setOptions(options);

        Question saved = questionRepository.save(question);

        resolveCorrectOption(saved, req.getCorrectOptionIndex());

        logAdminAction(AdminAction.ActionType.ADD_QUESTION,
                "Added question to exam [" +
                        exam.getTitle() + "]");

        log.info("Question added to exam [{}] id=[{}]",
                exam.getTitle(), saved.getId());

        return mapToResponse(saved, true);
    }

    // ═════════════════════════════════════════════════════════════
    // BULK ADD
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public List<QuestionResponse> addBulkQuestions(
            Long examId, BulkQuestionRequest req) {

        findExamOrThrow(examId);

        int currentMax =
                questionRepository.findMaxOrderByExamId(examId);

        List<QuestionResponse> responses = new ArrayList<>();

        for (int i = 0; i < req.getQuestions().size(); i++) {
            QuestionRequest qReq = req.getQuestions().get(i);
            if (qReq.getQuestionOrder() <= 0) {
                qReq.setQuestionOrder(currentMax + i + 1);
            }
            responses.add(addQuestion(examId, qReq));
        }

        log.info("Bulk added [{}] questions to exam [{}]",
                responses.size(), examId);

        return responses;
    }

    // ═════════════════════════════════════════════════════════════
    // UPDATE QUESTION
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public QuestionResponse updateQuestion(Long questionId,
                                           QuestionRequest req) {

        Question question = findQuestionOrThrow(questionId);

        validateOptionLabels(req);

        question.setContent(req.getContent().trim());
        question.setCodeSnippet(req.getCodeSnippet());
        question.setLanguage(normalizeLanguage(req.getLanguage()));
        question.setQuestionType(normalizeType(
                req.getQuestionType()));
        question.setMarks(req.getMarks() > 0
                ? req.getMarks() : 1);
        question.setNegativeMarks(req.getNegativeMarks());
        question.setQuestionOrder(req.getQuestionOrder());
        question.setExplanation(req.getExplaination());

        // Replace all options
        question.getOptions().clear();
        question.getOptions().addAll(
                buildOptions(req.getOptions(), question));

        Question saved = questionRepository.save(question);

        resolveCorrectOption(saved, req.getCorrectOptionIndex());

        logAdminAction(AdminAction.ActionType.UPDATE_QUESTION,
                "Updated question id=[" + questionId + "]");

        log.info("Question [{}] updated", questionId);

        return mapToResponse(saved, true);
    }

    // ═════════════════════════════════════════════════════════════
    // DELETE QUESTION
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = findQuestionOrThrow(questionId);
        Long examId       = question.getExam().getId();
        int  deletedOrder = question.getQuestionOrder();

        questionRepository.delete(question);

        // Shift remaining questions up by 1
        questionRepository.shiftOrdersAfterDelete(
                examId, deletedOrder);

        logAdminAction(AdminAction.ActionType.DELETE_QUESTION,
                "Deleted question id=[" + questionId + "]");

        log.info("Question [{}] deleted, orders shifted",
                questionId);
    }

    // ═════════════════════════════════════════════════════════════
    // REORDER QUESTIONS
    // ═════════════════════════════════════════════════════════════

    @Transactional
    public List<QuestionSummaryResponse> reorderQuestions(
            Long examId, ReorderQuestionsRequest req) {

        findExamOrThrow(examId);

        List<Long> ids = req.getOrderedQuestionIds();

        for (int i = 0; i < ids.size(); i++) {
            Question q = findQuestionOrThrow(ids.get(i));

            if (!q.getExam().getId().equals(examId))
                throw new ValidationException(
                        "Question [" + ids.get(i) +
                                "] does not belong to exam [" +
                                examId + "].");

            q.setQuestionOrder(i + 1);
            questionRepository.save(q);
        }

        logAdminAction(AdminAction.ActionType.REORDER_QUESTIONS,
                "Reordered questions for exam [" +
                        examId + "]");

        log.info("Questions reordered for exam [{}]", examId);

        return questionRepository
                .findByExamIdOrderByQuestionOrder(examId)
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    // ═════════════════════════════════════════════════════════════
    // READ — USER FACING (no answers)
    // ═════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsForUser(
            Long examId) {
        findExamOrThrow(examId);
        return questionRepository
                .findByExamIdOrderByQuestionOrder(examId)
                .stream()
                .map(q -> mapToResponse(q, false))
                .collect(Collectors.toList());
    }

    // ═════════════════════════════════════════════════════════════
    // READ — ADMIN FACING (with answers)
    // ═════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsForAdmin(
            Long examId) {
        findExamOrThrow(examId);
        return questionRepository
                .findByExamIdOrderByQuestionOrder(examId)
                .stream()
                .map(q -> mapToResponse(q, true))
                .collect(Collectors.toList());
    }

    // ── Summaries ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<QuestionSummaryResponse> getQuestionSummaries(
            Long examId) {
        findExamOrThrow(examId);
        return questionRepository
                .findByExamIdOrderByQuestionOrder(examId)
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    // ── By language ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<QuestionResponse> getByLanguage(
            Long examId, String language) {
        findExamOrThrow(examId);
        return questionRepository
                .findByExamIdAndLanguage(
                        examId, normalizeLanguage(language))
                .stream()
                .map(q -> mapToResponse(q, false))
                .collect(Collectors.toList());
    }

    // ── By type ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<QuestionResponse> getByType(
            Long examId, String type) {
        findExamOrThrow(examId);
        return questionRepository
                .findByExamIdAndType(
                        examId, normalizeType(type))
                .stream()
                .map(q -> mapToResponse(q, false))
                .collect(Collectors.toList());
    }

    // ── By language + type ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<QuestionResponse> getByLanguageAndType(
            Long examId, String language, String type) {
        findExamOrThrow(examId);
        return questionRepository
                .findByExamIdAndLanguageAndType(
                        examId,
                        normalizeLanguage(language),
                        normalizeType(type))
                .stream()
                .map(q -> mapToResponse(q, false))
                .collect(Collectors.toList());
    }

    // ── Combined filter ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<QuestionResponse> filterQuestions(
            Long examId,
            QuestionFilterRequest filter,
            boolean includeAnswers) {

        findExamOrThrow(examId);

        List<Question> questions;

        if (filter.getKeyword() != null
                && !filter.getKeyword().isBlank()) {
            questions = questionRepository
                    .searchByContent(examId, filter.getKeyword());
        } else if (filter.getLanguage() != null
                && filter.getQuestionType() != null) {
            questions = questionRepository
                    .findByExamIdAndLanguageAndType(
                            examId,
                            normalizeLanguage(filter.getLanguage()),
                            normalizeType(filter.getQuestionType()));
        } else if (filter.getLanguage() != null) {
            questions = questionRepository
                    .findByExamIdAndLanguage(
                            examId,
                            normalizeLanguage(filter.getLanguage()));
        } else if (filter.getQuestionType() != null) {
            questions = questionRepository
                    .findByExamIdAndType(
                            examId,
                            normalizeType(filter.getQuestionType()));
        } else if (Boolean.TRUE.equals(filter.getAiGenerated())) {
            questions = questionRepository
                    .findAiGeneratedByExamId(examId);
        } else {
            questions = questionRepository
                    .findByExamIdOrderByQuestionOrder(examId);
        }

        return questions.stream()
                .map(q -> mapToResponse(q, includeAnswers))
                .collect(Collectors.toList());
    }

    // ── Single question by id (admin) ─────────────────────────────
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long questionId) {
        return mapToResponse(
                findQuestionOrThrow(questionId), true);
    }

    // ── Count ─────────────────────────────────────────────────────
    public int countByExam(Long examId) {
        return questionRepository.countByExamId(examId);
    }

    // ── Total marks sum ───────────────────────────────────────────
    public int sumMarksByExam(Long examId) {
        return questionRepository.sumMarksByExamId(examId);
    }

    // ── Stats ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public QuestionStatsResponse getQuestionStats(Long examId) {
        Exam exam = findExamOrThrow(examId);

        int total = questionRepository.countByExamId(examId);
        int marks = questionRepository.sumMarksByExamId(examId);
        int aiCount = questionRepository
                .findAiGeneratedByExamId(examId).size();

        return QuestionStatsResponse.builder()
                .examId(exam.getId())
                .examTitle(exam.getTitle())
                .totalQuestions(total)
                .totalMarks(marks)
                .javaQuestions(questionRepository
                        .countByExamIdAndLanguage(examId, "java"))
                .pythonQuestions(questionRepository
                        .countByExamIdAndLanguage(examId, "python"))
                .csharpQuestions(questionRepository
                        .countByExamIdAndLanguage(examId, "csharp"))
                .sqlQuestions(questionRepository
                        .countByExamIdAndLanguage(examId, "sql"))
                .javascriptQuestions(questionRepository
                        .countByExamIdAndLanguage(
                                examId, "javascript"))
                .generalQuestions(questionRepository
                        .countByExamIdAndLanguage(examId, "general"))
                .codeQuestions(questionRepository
                        .countByExamIdAndType(examId, "CODE"))
                .conceptualQuestions(questionRepository
                        .countByExamIdAndType(examId, "CONCEPTUAL"))
                .scenarioQuestions(questionRepository
                        .countByExamIdAndType(examId, "SCENARIO"))
                .behavioralQuestions(questionRepository
                        .countByExamIdAndType(examId, "BEHAVIORAL"))
                .aiGeneratedCount(aiCount)
                .manualCount(total - aiCount)
                .build();
    }

    // ═════════════════════════════════════════════════════════════
    // MAPPING
    // ═════════════════════════════════════════════════════════════

    public QuestionResponse mapToResponse(Question q,
                                          boolean includeAnswers) {
        List<QuestionResponse.OptionResponse> opts =
                q.getOptions().stream()
                        .map(o -> QuestionResponse.OptionResponse
                                .builder()
                                .id(o.getId())
                                .optionLabel(o.getOptionLabel())
                                .optionText(o.getOptionText())
                                .optionCode(o.getOptionCode())
                                .build())
                        .collect(Collectors.toList());

        return QuestionResponse.builder()
                .id(q.getId())
                .content(q.getContent())
                .codeSnippet(q.getCodeSnippet())
                .language(q.getLanguage())
                .questionType(q.getQuestionType())
                .marks(q.getMarks())
                .negativeMarks(q.getNegativeMarks())
                .questionOrder(q.getQuestionOrder())
                .aiGenerated(q.isAiGenerated())
                .options(opts)
                // Only expose correct answer to admin
                .correctOptionId(includeAnswers
                        ? q.getCorrectOptionId() : null)
                .explanation(includeAnswers
                        ? q.getExplanation() : null)
                .build();
    }

    public QuestionSummaryResponse mapToSummaryResponse(
            Question q) {
        return QuestionSummaryResponse.builder()
                .id(q.getId())
                .content(q.getContent())
                .language(q.getLanguage())
                .questionType(q.getQuestionType())
                .marks(q.getMarks())
                .negativeMarks(q.getNegativeMarks())
                .questionOrder(q.getQuestionOrder())
                .optionCount(q.getOptions() != null
                        ? q.getOptions().size() : 0)
                .aiGenerated(q.isAiGenerated())
                .build();
    }

    // ═════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════

    private Question findQuestionOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + id));
    }

    private Exam findExamOrThrow(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exam not found with id: " + examId));
    }

    private List<Option> buildOptions(
            List<QuestionRequest.OptionRequest> requests,
            Question question) {
        return requests.stream()
                .map(or -> Option.builder()
                        .question(question)
                        .optionLabel(
                                or.getOptionLabel().toUpperCase())
                        .optionText(or.getOptionText().trim())
                        .optionCode(or.getOptionCode())
                        .build())
                .collect(Collectors.toList());
    }

    private void resolveCorrectOption(Question saved, int idx) {
        List<Option> opts = saved.getOptions();

        if (opts == null || opts.isEmpty())
            throw new ValidationException(
                    "Options were not saved correctly.");

        if (idx < 0 || idx >= opts.size())
            throw new ValidationException(
                    "correctOptionIndex [" + idx +
                            "] out of range. Valid: 0–" +
                            (opts.size() - 1));

        // Sort A→D to match index expectation
        opts.sort((a, b) ->
                a.getOptionLabel().compareTo(b.getOptionLabel()));

        saved.setCorrectOptionId(opts.get(idx).getId());
        questionRepository.save(saved);
    }

    private void validateOptionLabels(QuestionRequest req) {
        long distinct = req.getOptions().stream()
                .map(o -> o.getOptionLabel().toUpperCase())
                .distinct()
                .count();

        if (distinct != req.getOptions().size())
            throw new ValidationException(
                    "Option labels must be unique (A, B, C, D).");
    }

    private String normalizeLanguage(String lang) {
        if (lang == null || lang.isBlank()) return "general";
        return switch (lang.toLowerCase().trim()) {
            case "java"              -> "java";
            case "python"            -> "python";
            case "csharp", "c#"     -> "csharp";
            case "javascript", "js" -> "javascript";
            case "sql"               -> "sql";
            case "c", "cpp", "c++" -> "cpp";
            case "kotlin"            -> "kotlin";
            case "typescript", "ts" -> "typescript";
            default                  -> "general";
        };
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) return "CONCEPTUAL";
        return switch (type.toUpperCase().trim()) {
            case "CODE"       -> "CODE";
            case "CONCEPTUAL" -> "CONCEPTUAL";
            case "SCENARIO"   -> "SCENARIO";
            case "BEHAVIORAL" -> "BEHAVIORAL";
            default           -> "CONCEPTUAL";
        };
    }

    private void logAdminAction(AdminAction.ActionType type,
                                String description) {
        try {
            String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();
            userRepository.findByEmail(email).ifPresent(admin -> {
                actionRepository.save(
                        AdminAction.builder()
                                .admin(admin)
                                .actionType(type)
                                .description(description)
                                .build());
            });
        } catch (Exception ex) {
            log.warn("Could not log admin action [{}]: {}",
                    type, ex.getMessage());
        }
    }
}