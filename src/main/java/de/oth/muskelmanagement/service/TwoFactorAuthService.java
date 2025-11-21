package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.User;

public interface TwoFactorAuthService {
    String generateAndSendCode(User user);
    
    boolean validateCode(Long userId, String code);
    
    void invalidateAllCodesForUser(Long userId);
    
    void cleanupExpiredCodes();
    
    int getRemainingAttempts(Long userId);
}