package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {

    List<TwoFactorCode> findByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    List<TwoFactorCode> findByUserIdAndUsedFalse(Long userId);

    Optional<TwoFactorCode> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);

    void deleteByUserId(Long userId);
}