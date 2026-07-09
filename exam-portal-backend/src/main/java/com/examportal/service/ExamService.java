package com.examportal.service;

import com.examportal.dto.request.ExamRequest;
import com.examportal.dto.response.ExamResponse;
import com.examportal.exception.ValidationException;
import com.examportal.model.AdminAction;
import com.examportal.model.Exam;
import com.examportal.repository.AdminActionRepository;
import com.examportal.repository.ExamAttemptRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);
    private final ExamRepository examRepository;
     private final ExamAttemptRepository attemptRepository;
     private final QuestionService questionService;
     private final UserRepository  userRepository;
     private final AdminActionRepository actionRepository;


     private ExamResponse createExam(ExamRequest req){

         validatePassingMarks(req.getPassingMarks(),
                                req.getTotalMarks());
         if (examRepository.existsByTitleIgnoreCase(req.getTitle())) {

             throw new ValidationException("An exam with title [" + req.getTitle() +"] already exists.")
         }

         Exam exam = Exam.builder()
                 .title(req.getTitle().trim())
                 .description(req.getDescription())
                 .category(req.getCategory().trim())
                 .durationMintues(req.getDurationMintues()).totalMarks(req.getTotalMarks())
                 .passingMarks(req.getPassingMarks())
                 .difficulty(parseDifficulty(req.getDifficulty()))
                 .active(req.isActive())
                 .instructions(req.getInstructions())
                 .thumbnailUrl(req.getThumbnailUrl())
                 .tags(req.getTags())
                 .build();

         Exam saved = examRepository.save(exam);
         if (req.getQuestions() != null && !req.getQuestions().isEmpty()) {
             req.getQuestions().forEach(q -> questionService.addQuestion(saved.getId(), q);
         }

         logAdminAction(AdminAction.ActionType.CREATE_EXAM,"Created exam : "+ saved.getTitle());
         log.info("Exam created :"+ saved.getTitle());
         log.info("Exam created : [{}] id = [{}]",saved.getTitle(),saved.getId());
         return mapToFullResponse(examRepository.findById(saved.getId()).orElse(saved),true,true);
     }
}
