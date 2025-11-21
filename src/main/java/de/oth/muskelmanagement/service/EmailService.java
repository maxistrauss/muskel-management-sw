package de.oth.muskelmanagement.service;

public interface EmailService {
    void sendTwoFactorCode(String toEmail, String code, String userName);
}