package com.examportal.service;

import com.examportal.dto.request.BulkQuestionRequest;
import com.examportal.dto.request.QuestionRequest;
import com.examportal.dto.response.QuestionResponse;
import com.examportal.model.AdminAction;
import com.examportal.model.Exam;
import com.examportal.model.Option;
import com.examportal.model.Question;
import com.examportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final  QuestionRepository  questionRepository;
     private final  OptionRepository    optionRepository;
     private final  ExamRepository      examRepository;
     private final  UserRepository      userRepository;
     private final AdminActionRepository adminActionRepository;

     @Transactional
     public QuestionResponse  addQuestion(Long  examId, QuestionRequest req) {

         Exam  exam  =  findExamOrThrow(examId);

         validateOptionLabels(req);
         int  order  = req.getQuestionOrder() > 0
                 ? req.getQuestionOrder()
                 : questionRepository.findMaxOrderByExamId(examId) + 1;
         Question  question = Question.builder()
                 .exam(exam)
                 .content(req.getContent().trim())
                 .codeSnippet(req.getCodeSnippet())
                 .language(normalizeType(req.getQuestionType()))
                 .marks(req.getMarks() > 0 ? req.getMarks() : 1)
                 .negativeMarks(req.getNegativeMarks())
                 .questionOrder(order)
                 .explanation(req.getExplaination())
                 .aiGenerated(req.isAiGenerated())
                 .build();

         List<Option>  options  = buildOptions(req.getOptions(), question);
         question.setOptions(options);
         Question saved  = questionRepository.save(question);
         resolveCorrectOption(saved, req.getCorrectOptionIndex());

         logAdminAction(AdminAction.ActionType.ADD_QUESTION, "Added question to exam [" + exam.getTitle() + "]");
         log.info("Question added to  exam [{]] id = [{]]", exam.getTitle(), saved.getId());
         return mapToResponse(saved,true);
     }

     @Transactional
     public  List<QuestionResponse>  addBulkQuestions(Long  examId, BulkQuestionRequest  req) {

          findExamOrThrow(examId);
          int  currentMax  = questionRepository.findMaxOrderByExamId(examId);
          List<QuestionResponse>  responses  = new ArrayList<>();

         for (int i = 0; i < req.getQuestions().size(); i++) {

             QuestionRequest  qReq = req.getQuestions().get(i);
             if (qReq.getQuestionOrder() <= 0) {

                 qReq.setQuestionOrder(currentMax + i + 1);
             }
             responses.add(addQuestion(examId, qReq));
         }
         log.info("Bulk added [{]] questions to exam [{]]", responses.size(),examId);
         return responses;
     }

     @Transactional
     public  QuestionResponse  updateQuestion(Long  questionId, QuestionRequest  req){

         Question  question  = findQuestionOrThrow(questionId);
         validateOptionLabels(req);

         question.setContent(req.getContent().trim());
         question.setCodeSnippet(req.getCodeSnippet());
         question.setLanguage(normalizeLanguage(req.getLanguage()));
         question.setQuestionType(normalizeType(req.getQuestionType()));
         question.setMarks(req.getMarks() > 0 ? req.getMarks() : 1);
         question.setNegativeMarks(req.getNegativeMarks());
         question.setQuestionOrder(req.getQuestionOrder());
         question.setExplanation(req.getExplaination());

         question.getOptions().clear();
         question.getOptions().addAll(buildOptions(req.getOptions(),question));

         Question saved  = questionRepository.save(question);
         resolveCorrectOption(saved,req.getCorrectOptionIndex());

         logAdminAction(AdminAction.ActionType.UPDATE_EXAM, "Updated question id = [" + questionId + "]");
         log.info("Question [{]] updated ",questionId);
         return  mapToResponse(saved,true);
     }
}
