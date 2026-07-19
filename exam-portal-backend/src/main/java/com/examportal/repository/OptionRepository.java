package com.examportal.repository;

import com.examportal.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option,Long> {

    List<Option> findByQuestionIdOrderByOptionLabel(Long questionId);

    List<Option>  findByQuestionIdAndOptionLabel(Long questionId,String label);

    int countByQuestionId(Long questionId);

    @Modifying
    @Query("DELETE FROM Option o " + "WHERE o.question.id  = :questionId")
    void deleteAllByQuestionId(Long questionId);

    @Modifying
    @Query("DELETE FROM Option o " + "WHERE o.question.exam.id  = :examId")
    void deleteAllByExamId(Long examId);
}
