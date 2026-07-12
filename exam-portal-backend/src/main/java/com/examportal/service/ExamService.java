package com.examportal.service;

import com.examportal.dto.request.ExamFilterRequest;
import com.examportal.dto.request.ExamRequest;
import com.examportal.dto.response.ExamDetailResponse;
import com.examportal.dto.response.ExamResponse;
import com.examportal.dto.response.ExamStatsResponse;
import com.examportal.dto.response.ExamSummaryResponse;
import com.examportal.exception.ValidationException;
import com.examportal.model.AdminAction.ActionType;
import com.examportal.model.Exam;
import com.examportal.repository.AdminActionRepository;
import com.examportal.repository.ExamAttemptRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);
    private final ExamRepository examRepository;
     private final ExamAttemptRepository attemptRepository;
     private final QuestionService questionService;
     private final UserRepository  userRepository;
     private final AdminActionRepository actionRepository;


     @Transactional
     public ExamResponse createExam(ExamRequest req){

         validatePassingMarks(req.getPassingMarks(),
                                req.getTotalMarks());
         if (examRepository.existsByTitleIgnoreCase(req.getTitle())) {

             throw new ValidationException("An exam with title [" + req.getTitle() +"] already exists.");
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
             req.getQuestions().forEach(q -> questionService.addQuestion(saved.getId(), q));
         }

         logAdminAction(ActionType.CREATE_EXAM,"Created exam : "+ saved.getTitle());
         log.info("Exam created :"+ saved.getTitle());
         log.info("Exam created : [{}] id = [{}]",saved.getTitle(),saved.getId());
         return mapToFullResponse(examRepository.findById(saved.getId()).orElse(saved),true,true);
     }

     @Transactional
     public ExamResponse updateExam(Long id, ExamRequest req){

         Exam exam = findOrThrow(id);
         validatePassingMarks(req.getPassingMarks(),
                 req.getTotalMarks());
         if (examRepository.existsByTitleIgnoreCaseAndIdNot(req.getTitle(),id)) {
             throw  new ValidatioonException("Another exam with title ["+ req.getTitle() +"] already exists.");
         }

         exam.setTitle(req.getTitle().trim());
         exam.setDescription(req.getDescription());
         exam.setCategory(req.getCategory().trim());
         exam.setDurationMintues(req.getDurationMintues());
         exam.setTotalMarks(req.getTotalMarks());
         exam.setPassingMarks(req.getPassingMarks());
         exam.setDifficulty(parseDifficulty(req.getDifficulty()));
         exam.setActive(req.isActive());
         exam.setInstructions(req.getInstructions());
         exam.setThumbnailUrl(req.getThumbnailUrl());
         exam.setTags(req.getTags());

         Exam updated = examRepository.save(exam);
         logAdminAction(ActionType.UPDATE_EXAM,"Updated exam :"+updated.getTitle());
         log.info("Exam updated : [{}] id = [{}]",updated.getId());
         return mapToFullResponse(updated,true,true);
     }

     @Transactional
     public void deleteExam(Long id){

         Exam exam = findOrThrow(id);
         logAdminAction(ActionType.DELETE_EXAM,
                 "Deleted exam : "+exam.getTitle());
         examRepository.delete(exam);
         log.info("Exam deleted :[{}] id = [{]]",exam.getTitle(),id);
     }

     @Transactional
     public ExamSummaryResponse toggleStatus(Long id){
         Exam exam = findOrThrow(id);
         exam.setActive(!exam.isActive());
         Exam  saved = examRepository.save(exam);

         logAdminAction(ActionType.TOGGLE_EXAM,"Toggled exam [" + saved.getTitle() + "] -> active = "+ saved.isActive());
         log.info("Exam [{}] toggled -> active ={}",saved.getTitle(),saved.isActive());

         return mapToSummaryResponse(saved);
     }

     @Transactional(readOnly = true)
     public List<ExamSummaryResponse> getAllActiveExams(){
         return examRepository.findByActiveTrueOrderByCreatedAtDesc()
                 .stream()
                 .map(this::mapToSummaryResponse)
                 .collect(Collectors.toList());
     }

     @Transactional(readOnly = true)
     public List<ExamSummaryResponse> getActiveByCategory(String category){

         return examRepository.findByCategoryAndActiveTrueOrderByCreatedAtDesc(category)
                 .stream()
                 .map(this::mapToSummaryResponse)
                 .collect(Collectors.toList());
     }

     @Transactional(readOnly = true)
     public List<ExamSummaryResponse> getActiveByDifficulty(String difficulty){


         return examRepository.findByDifficultyAndActiveTrueOrderByCreatedAtDesc(parseDifficulty(difficulty))
                 .stream()
                 .map(this::mapToSummaryResponse)
                 .collect(Collectors.toList());
     }

     @Transactional(readOnly = true)
     public List<ExamSummaryResponse>  searchActiveExams(String keyword){

         return examRepository
                 .searchActiveExams(keyword)
                 .stream()
                 .map(this::mapToSummaryResponse)
                 .collect(Collectors.toList());

     }

     @Transactional(readOnly = true)
     public List<ExamSummaryResponse>  filterExams(ExamFilterRequest filter){

         List<Exam> exams;
         boolean activeOnly  = filter.getActiveOnly() == null || filter.getActiveOnly();

         if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {

             exams = activeOnly ? examRepository.searchActiveExams(filter.getKeyword()) : examRepository.searchAllExams(filter.getKeyword());
         } else if (filter.getCategory() != null && filter.getDifficulty() != null) {

             exams = examRepository.findByCategoryAndDifficultyAndActive(filter.getCategory(),parseDifficulty(filter.getDifficulty()));
         } else if (filter.getDifficulty() != null) {

             exams = examRepository.findByDifficultyAndActiveTrueOrderByCreatedAtDesc(parseDifficulty(filter.getDifficulty()));
         } else if (filter.getDifficulty() != null) {

             exams = examRepository.findByDifficultyAndActiveTrueOrderByCreatedAtDesc(parseDifficulty(filter.getDifficulty()));
         }
         else{

             exams = examRepository.findByActiveTrueOrderByCreatedAtDesc();
         }

         return exams.stream()
                 .map(this::mapToSummaryResponse)
                 .collect(Collectors.toList());
     }

     @Transactional(readOnly = true)
     public ExamDetailResponse getExamForUser(Long id){

         Exam exam = findOrThrow(id);
         if (!exam.isActive()) {

             throw new ValidationException("This exam is not currently available.");
         }
         return mapToDetailResponse(exam);
     }

     @Transactional(readOnly = true)
     public List<String>  getActiveCategories(){
         return examRepository.findDistinctActiveCategories();
     }

     @Transactional(readOnly = true)
     public List<ExamResponse>  getAllExamsAdmin(){

         return  examRepository.findAllByOrderByCreatedAtDesc()
                 .stream()
                 .map(e -> mapToFullResponse(e,false,true))
                 .collect(Collectors.toList());
     }

     @Transactional(readOnly = true)
     public ExamResponse getExamForAdmin(Long id){

         return mapToFullResponse(findOrThrow(id),true,true);
     }

     @Transactional(readOnly = true)
     public List<String> getAllCategories(){

         return examRepository.findAllDistinctActiveCategories();
     }

     public ExamStatsResponse getExamStats(Long id){
         Exam exam = findOrThrow(id);

         long  totalAttempts = attemptRepository.countByExamId(id);
         long  totalPassed  = attemptRepository.countPassedByExamId(id);
         long totalFailed = totalAttempts - totalPassed;
         Double avgScore = attemptRepository.avgScoreByExamId(id);
         Double avgPct = attemptRepository.avgPercentageByExamId(id);
         Double  highestScore = attemptRepository.maxScoreByExamId(id);
     }
}
