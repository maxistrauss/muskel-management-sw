package de.oth.muskelmanagement.service;

import java.time.LocalDate;

public interface EmailService {
    void sendTwoFactorCode(String toEmail, String code, String userName);
    
    void sendAccountDeactivationEmail(String toEmail, String userName, String reason);
    
    void sendAccountActivationEmail(String toEmail, String userName);
    
    void sendSubscriptionExpiryReminder(String toEmail, String userName, String tarifName, LocalDate expiryDate, long daysRemaining);
}