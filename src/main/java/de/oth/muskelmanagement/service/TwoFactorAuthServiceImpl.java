package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.TwoFactorCode;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.repository.TwoFactorCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private TwoFactorCodeRepository codeRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.2fa.code-validity-minutes:5}")
    private int codeValidityMinutes;

    @Value("${app.2fa.max-attempts:3}")
    private int maxAttempts;

    @Override
    @Transactional
    public String generateAndSendCode(User user) {
        logger.info("Generating 2FA code for user: {}", user.getEmail());

        // Invalidate all existing codes for this user
        invalidateAllCodesForUser(user.getId());

        // Generate a new 4-digit code
        String code = generateRandomCode();

        // Create and save the code
        TwoFactorCode twoFactorCode = new TwoFactorCode();
        twoFactorCode.setUserId(user.getId());
        twoFactorCode.setCode(code);
        twoFactorCode.setExpiryTime(LocalDateTime.now().plusMinutes(codeValidityMinutes));
        twoFactorCode.setCreatedAt(LocalDateTime.now());
        codeRepository.save(twoFactorCode);

        // Send email with the code
        try {
            emailService.sendTwoFactorCode(user.getEmail(), code, user.getFirstName());
            logger.info("2FA code sent successfully to user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send 2FA code to user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification code", e);
        }

        return code;
    }

    @Override
    @Transactional
    public boolean validateCode(Long userId, String code) {
        logger.debug("Validating 2FA code for user ID: {}", userId);

        if (code == null || code.trim().isEmpty()) {
            logger.warn("Empty code provided for user ID: {}", userId);
            return false;
        }

        // Get the most recent unused code for the user
        Optional<TwoFactorCode> optionalCode = codeRepository
                .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(userId);

        if (optionalCode.isEmpty()) {
            logger.warn("No active code found for user ID: {}", userId);
            return false;
        }

        TwoFactorCode twoFactorCode = optionalCode.get();

        // Check if code is expired
        if (twoFactorCode.isExpired()) {
            logger.info("Code expired for user ID: {}", userId);
            twoFactorCode.setUsed(true);
            codeRepository.save(twoFactorCode);
            return false;
        }

        // Check if max attempts reached
        if (twoFactorCode.getAttempts() >= maxAttempts) {
            logger.warn("Max attempts reached for user ID: {}", userId);
            twoFactorCode.setUsed(true);
            codeRepository.save(twoFactorCode);
            return false;
        }

        // Increment attempts
        twoFactorCode.setAttempts(twoFactorCode.getAttempts() + 1);

        // Validate the code
        boolean isValid = twoFactorCode.getCode().equals(code.trim());

        if (isValid) {
            logger.info("Valid code provided for user ID: {}", userId);
            twoFactorCode.setUsed(true);
        } else {
            logger.warn("Invalid code provided for user ID: {}. Attempt: {}/{}", 
                       userId, twoFactorCode.getAttempts(), maxAttempts);
        }

        codeRepository.save(twoFactorCode);
        return isValid;
    }

    @Override
    @Transactional
    public void invalidateAllCodesForUser(Long userId) {
        logger.debug("Invalidating all codes for user ID: {}", userId);
        List<TwoFactorCode> codes = codeRepository.findByUserIdAndUsedFalse(userId);
        codes.forEach(code -> code.setUsed(true));
        codeRepository.saveAll(codes);
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredCodes() {
        logger.info("Running cleanup of expired 2FA codes");
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24);
        codeRepository.deleteByCreatedAtBefore(cutoffDate);
        logger.info("Cleanup completed");
    }

    @Override
    public int getRemainingAttempts(Long userId) {
        Optional<TwoFactorCode> optionalCode = codeRepository
                .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(userId);

        if (optionalCode.isEmpty()) {
            return 0;
        }

        TwoFactorCode code = optionalCode.get();
        if (code.isExpired() || code.getAttempts() >= maxAttempts) {
            return 0;
        }

        return maxAttempts - code.getAttempts();
    }

    private String generateRandomCode() {
        // Generate a random 4-digit code (1000-9999)
        int code = 1000 + RANDOM.nextInt(9000);
        return String.valueOf(code);
    }
}