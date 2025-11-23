package de.oth.muskelmanagement.service;

public interface EmailService {
    void sendTwoFactorCode(String toEmail, String code, String userName);
    
    void sendAccountDeactivationEmail(String toEmail, String userName, String reason);
    
    void sendAccountActivationEmail(String toEmail, String userName);
}