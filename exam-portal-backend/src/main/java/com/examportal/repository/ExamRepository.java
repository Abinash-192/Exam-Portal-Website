package com.examportal.repository;

import com.examportal.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam,Long> {


    List<Exam>  findByActiveTrueOrderByCreatedAtDesc();

    List<Exam>  findByCategoryAndActiveTrueOrderByCreatedAtDesc(String category);

    List<Exam>  findByCategoryOrderByCreatedAtDesc(String category);

    List<Exam>  findByDifficultyAndActiveTrueOrderByCreatedAtDesc(Exam.DifficultyLevel difficulty);

    @Query("SELECT e FROM Exam e " +
            "WHERE e.active  = true "+
            "AND e.category  = :category" +
            "AND  e.difficulty  = :difficulty" +
            "ORDER  BY e.createdAtAt  DESC")

    List<Exam>  findByCategoryAndDifficultyAndActive(String category, Exam.DifficultyLevel difficulty);

    List<Exam>  findAllByOrderByCreatedAtDesc();

    List<String>  findDistinctActiveCategories();

    long  countActiveExams();

    long  countByCategoryAndActive(String category);

    List<Exam>  searchActiveExams(String keyword);

    List<Exam>  searchAllExams(String keyword);

    void  updateActiveStatus(Long id, boolean status);

    boolean  existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    List<Exam>  findAllByIds(List<Long> ids);


}
