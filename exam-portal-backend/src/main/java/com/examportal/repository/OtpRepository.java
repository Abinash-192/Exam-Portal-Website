package com.examportal.repository;

import com.examportal.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification,Long> {

    Optional<OtpVerification> findTopByIdentifierAndTypeUsedFalseOrderByCreatedAtDesc(String identifier, OtpVerification.OtpType type);

    @Modifying
    @Query("DELETE FROM OtpVerication o WHERE o.identifier = :identifier AND O.type = :type")
    void deleteIdentifierAndType(String identifier , OtpVerification.OtpType type);

    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now")
    void deleteAllExpiredBefore(LocalDateTime now);
}
