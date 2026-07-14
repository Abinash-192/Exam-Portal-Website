package com.examportal.service;

import com.examportal.dto.request.ExamFilterRequest;
import com.examportal.dto.request.ExamRequest;
import com.examportal.dto.response.ExamDetailResponse;
import com.examportal.dto.response.ExamResponse;
import com.examportal.dto.response.ExamStatsResponse;
import com.examportal.dto.response.ExamSummaryResponse;
import com.examportal.exception.ResourceNotFoundException;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
             throw  new ValidationException("Another exam with title ["+ req.getTitle() +"] already exists.");
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
         Integer  highestScore = attemptRepository.maxScoreByExamId(id);
         Integer  lowestScore = attemptRepository.minScoreByExamId(id);

         double passRate  = totalAttempts > 0 ? Math.round(totalPassed * 100.0 / totalAttempts * 10) / 10.0 : 0.0;
         return ExamStatsResponse.builder()
                 .examId(exam.getId())
                 .examTitle(exam.getTitle())
                 .category(exam.getCategory())
                 .difficulty(exam.getDifficulty().name())
                 .totalAttempts(totalAttempts)
                 .totalPassed(totalPassed)
                 .totalFailed(totalFailed)
                 .passRate(passRate)
                 .totalMarks(exam.getTotalMarks())
                 .passingMarks(exam.getPassingMarks())
                 .avgScore(round(avgScore))
                 .avgPercentage(round(avgPct))
                 .highestScore(highestScore != null ? highestScore : 0)
                 .lowestScore(lowestScore != null ? lowestScore : 0)
                 .build();
     }

     public long countActiveExams(){
         return examRepository.countActiveExams();
     }

     public long countAllExams(){
         return examRepository.count();
     }

     public ExamResponse mapToFullResponse(Exam exam, boolean includeQuestions,boolean includeStats){

         ExamResponse.ExamResponseBuilder builder = ExamResponse.builder()
                 .id(exam.getId())
                 .title(exam.getTitle())
                 .description(exam.getDescription())
                 .category(exam.getCategory())
                 .difficulty(exam.getDifficulty().name())
                 .active(exam.isActive())
                 .instructions(exam.getInstructions())
                 .thumbnail(exam.getThumbnailUrl())
                 .tags(exam.getTags())
                 .durationMintues(exam.getDurationMintues())
                 .totalMarks(exam.getTotalMarks())
                 .passingMarks(exam.getPassingMarks())
                 .questionCount(exam.getQuestions() != null ? exam.getQuestions().size() : 0)
                 .createdAt(exam.getCreatedAt())
                 .updatedAt(exam.getUpdatedAt());

         if (includeStats) {

             Long totalAttempts = attemptRepository.countByExamId(exam.getId());
             Long totalPassed = attemptRepository.countPassedByExamId(exam.getId());
             Double avgScore = attemptRepository.avgScoreByExamId(exam.getId());
             Double avgPct = attemptRepository.avgPercentageByExamId(exam.getId());
             double passRate = totalAttempts != null && totalAttempts > 0 ? Math.round(totalPassed * 100.0 / totalAttempts * 10) / 10.0 : 0.0;
             builder.totalAttempts(totalAttempts)
                     .totalPassed(totalPassed)
                     .avgscore(round(avgScore))
                     .avgPecentage(round(avgPct))
                     .passRate(passRate);
         }

         if (includeQuestions && exam.getQuestions() != null) {

             builder.questions(exam.getQuestions().stream().map(q -> questionService.mapToResponse(q,includeStats)).collect(Collectors.toList()));
         }
         return builder.build();
     }

     public ExamSummaryResponse mapToSummaryResponse(Exam exam){

         return  ExamSummaryResponse.builder()
                 .id(exam.getId())
                 .title(exam.getTitle())
                 .description(exam.getDescription())
                 .category(exam.getCategory())
                 .difficulty(exam.getDifficulty().name())
                 .active(exam.isActive())
                 .thumbnailUrl(exam.getThumbnailUrl())
                 .tags(exam.getTags())
                 .durationMinutes(exam.getDurationMintues())
                 .totalMarks(exam.getTotalMarks())
                 .passingMarks(exam.getPassingMarks())
                 .questionCount(exam.getQuestions() != null ? exam.getQuestions().size() : 0)
                 .createdAt(exam.getCreatedAt())
                 .build();
     }

     public ExamDetailResponse mapToDetailResponse(Exam exam){

          return ExamDetailResponse.builder()
                  .id(exam.getId())
                  .title(exam.getTitle())
                  .description(exam.getDescription())
                  .category(exam.getCategory())
                  .difficulty(exam.getDifficulty().name())
                  .instructions(exam.getInstructions())
                  .thumbnailUrl(exam.getThumbnailUrl())
                  .tags(exam.getTags())
                  .durationMintues(exam.getDurationMintues())
                  .totalMarks(exam.getTotalMarks())
                  .passingMarks(exam.getPassingMarks())
                  .questionCount(exam.getQuestions() != null ? exam.getQuestions().size() : 0)
                  .createdAt(exam.getCreatedAt())
                  .questions(exam.getQuestions() != null ? exam.getQuestions().stream().map(q -> questionService.mapToResponse(q,false))
                          .collect(Collectors.toList()) : List.of())
                  .build();
     }

     public Exam findOrThrow(Long id){

         return  examRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exam not found with id :"+ id));
     }

     private  void validatePassingMarks(int passing, int total){

         if (passing > total) {

             throw new ValidationException("Passing marks (" + passing +" ) can't exceed" + "total marks ("+ total +").");
             if (passing <= 0) {

                 throw new ValidationException("Passing marks must be greater than 0.");
             }
         }
     }

     private Exam.DifficultyLevel parseDifficulty(String raw){

         if (raw == null || raw.isBlank()) {

             return Exam.DifficultyLevel.MEDIUM;
         }

         try {
             return  Exam.DifficultyLevel.valueOf(raw.toUpperCase());
         } catch (IllegalArgumentException e) {
             throw new ValidationException("Invalid difficulty : [" + raw +"]. Use Easy, Medium or Hard.");
         }
     }

     private double round(Double value){

         return value != null ? Math.round(value * 10.0) / 10.0 : 0.0;
     }

     private void logAdminAction(ActionType type, String description){

         try {

             String email = SecurityContextHolder.getContext().getAuthentication().getName();
             userRepository.findByEmail(email).ifPresent(admin -> {
                 com.examportal.model.AdminAction action = com.examportal.model.AdminAction.builder()
                         .admin(admin)
                         .actionType(type)
                         .description(description)
                         .build();
                 actionRepository.save(action);
             });
         } catch (Exception e) {

             log.warn("Could not log admin action [{}] : {]", type,e.getMessage());
         }
     }

     @Transactional(readOnly = true)
     public  ExamSummaryResponse getExamSummary(Long id){

         return mapToSummaryResponse(findOrThrow(id));
     }

     public long countByCategoryAndActive(String category){

         return examRepository.countByCategoryAndActive(category);
     }

     @Transactional
     public ExamSummaryResponse activateExam(Long id){

         Exam exam = findOrThrow(id);
         if (exam.isActive()) {

             throw new ValidationException("Exam is already active.");
         }

         exam.setActive(true);
         Exam saved = examRepository.save(exam);
         logAdminAction(ActionType.TOGGLE_EXAM, "Activated exam : " + saved.getTitle());
         log.info("Exam [{}]  activated ", saved.getTitle());
         return  mapToSummaryResponse(saved);
     }

     @Transactional
     public ExamSummaryResponse deactivateExam(Long id){

         Exam exam = findOrThrow(id);
         if (!exam.isActive()) {

             throw new ValidationException("Exam is already inactive");
             exam.setActive(false);
             Exam saved  = examRepository.save(exam);
             logAdminAction(ActionType.TOGGLE_EXAM, "Deactivated exam : " + saved.getTitle());
             log.info("Exam [{}] deactivated ", saved.getTitle());
             return  mapToSummaryResponse(saved);
         }
     }
}
