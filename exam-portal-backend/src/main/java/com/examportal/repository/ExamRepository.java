package com.examportal.repository;

import com.examportal.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            "WHERE e.active = true " +
            "AND e.category = :category " +
            "AND e.difficulty = :difficulty " +
            "ORDER BY e.createdAt DESC")
    List<Exam>  findByCategoryAndDifficultyAndActive(String category, Exam.DifficultyLevel difficulty);

    List<Exam>  findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT e.category " +
            "FROM Exam e WHERE e.active  = true " +
            "ORDER BY e.category ASC")
    List<String>  findDistinctActiveCategories();

    @Query("SELECT DISTINCT e.category "+
            "FROM Exam e ORDER BY e.category ASC")
    List<String>  findAllDistinctActiveCategories();

    @Query("SELECT COUNT(e) FROM Exam e WHERE e.active = true")
    long  countActiveExams();

    @Query("SELECT COUNT(e) FROM Exam e "+
            "WHERE e.category  = :category AND e.active  = true")
    long  countByCategoryAndActive(String category);

    @Query("SELECT e FROM Exam e "+
            "WHERE e.active  = true "+
            "AND (LOWER(e.title)  LIKE LOWER(CONCAT('%',:kw,'%')) "+
            "OR LOWER(e.description)  LIKE LOWER(CONCAT('%',:kw,'%'))"+
            " OR LOWER(e.tags)  LIKE LOWER(CONCAT('%',:kw,'%')))")
    List<Exam>  searchActiveExams(String keyword);

    @Query("SELECT e FROM Exam e "+
            "WHERE LOWER(e.title)  LIKE LOWER(CONCAT('%',:kw,'%'))" +
            "OR LOWER(e.description)  LIKE LOWER(CONCAT('%',:kw,'%'))" +
            "OR LOWER(e.tags) LIKE LOWER(CONCAT('%',:kw,'%'))")
    List<Exam>  searchAllExams(String keyword);

    @Modifying
    @Query("UPDATE Exam e SET e.active  = :status WHERE e.id = :id")
    void  updateActiveStatus(Long id, boolean status);

    boolean existsByTitleIgnoreCase(String title);

    @Query("SELECT COUNT(e) > 0 FROM Exam e "+
            "WHERE LOWER(e.title) = LOWER(:title) "+
            "AND  e.id <> :id" )
    boolean  existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    @Query("SELECT e FROM Exam e WHERE e.id IN :ids")
    List<Exam>  findAllByIds(List<Long> ids);


}
