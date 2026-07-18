package com.examportal.repository;

import com.examportal.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

     List<Question>  findByExamIdOrderByQuestionOrder(Long examId);

     @Query("SELECT COUNT(q) FROM Question q " + "WHERE q.exam.id = :examId")
     int countByExamId(Long examId);

     @Query("SELECT COUNT(q) FROM Question q " + "WHERE q.exam.id  = :examId " + "AND q.language =  :language")
     int countByExamIdAndLanguage(Long examId, String language);

     @Query("SELECT COUNT(q) FROM Question q " + "WHERE q.exam.id  =  :examId " + "AND  q.questionType  =  :type")
     int countByExamIdAndType(Long examId, String type);

     @Query("SELECT  COALESCE(MAX(q.questionOrder), 0) " + "FROM Question q WHERE q.exam.id  = :examId")
     int  findMaxOrderByExamId(Long  examId);

     @Modifying
     @Query("DELETE FROM Question q " + "WHERE q.exam.id  = :examId")
     void deleteAllByExamId(Long examId);

     @Query("SELECT q FROM Question q " + "WHERE q.exam.id  = :examId " + "AND q.language  = :language " + "ORDER BY q.questionOrder ASC")
     List<Question>  findByExamIdAndLanguage(Long examId, String language);

     @Query("SELECT q FROM Question q " + "WHERE q.exam.id  = :examId " + "AND q.questionType  = :type " + "ORDER BY q.questionOrder ASC")
     List<Question> findByExamIdAndType(Long examId, String type);

     @Query("SELECT q FROM Question q " + "WHERE q.exam.id  = :examId " + "AND q.language  = :language " + "AND q.questionType  = :type " + "ORDER BY q.questionOrder ASC")
     List<Question> findByExamIdAndLanguageAndType(Long examId, String language, String type);

     @Query("SELECT q FROM Question q " + "WHERE q.exam.id  = :examId " + "AND q.aiGenerated  = true " + "ORDER BY q.questionOrder ASC")
     List<Question> findAiGeneratedByExamId(Long examId);

     boolean existsByExamId(Long examId);

     @Modifying
     @Query("UPDATE Question q " + "SET q.questionOrder = q.questionOrder - 1 " + "WHERE q.exam.id  = :examId " + "AND q.questionOrder > :deleteOrder")
     void shiftOrdersAfterDelete(Long examId, int deletedOrder);

     @Query("SELECT q FROM Question q " + "WHERE q.exam.id  = :examId " + "AND LOWER(q.content) " + "LIKE LOWER(CONCAT('%', :keyword,'%'))")
     List<Question>  searchByContent(Long examId, String keyword);

     @Query("SELECT COALESCE(SUM(q.marks), 0)" + "FROM Question q WHERE q.exam.id = :examId")
     int sumMarksByExamId(Long examId);

}
