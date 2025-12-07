package de.oth.muskelmanagement.service;

import java.time.LocalDate;

public interface EmailService {
    void sendAccountDeactivationEmail(String toEmail, String userName, String deactivationReason);
    void sendAccountActivationEmail(String toEmail, String userName);

    void sendTwoFactorCodeEmail(String toEmail, String userName, String code);

    void sendSubscriptionExpiryReminder(String toEmail, String userName, String pricingName, LocalDate expiryDate,
            long daysRemaining);
}
