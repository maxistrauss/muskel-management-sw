package de.oth.muskelmanagement.service;

import java.time.LocalDate;

public interface EmailService {
    void sendAccountDeactivationEmail(String toEmail, String userName, String deactivationReason);
    void sendAccountActivationEmail(String toEmail, String userName);

    void sendTwoFactorCodeEmail(String toEmail, String userName, String code);

    void sendSubscriptionExpiryReminder(String toEmail, String userName, String pricingName, LocalDate expiryDate,
            long daysRemaining);

    void sendRegistrationConfirmationEmail(String toEmail, String userName);

    void sendPasswordChangeConfirmationEmail(String toEmail, String userName);

    void sendCourseEnrollmentConfirmationEmail(String toEmail, String userName, String courseName);

    void sendCourseEnrollmentCancelledEmail(String toEmail, String userName, String courseName);

    void sendCourseWaitlistConfirmationEmail(String toEmail, String userName, String courseName);

    void sendCourseWaitlistPromotionEmail(String toEmail, String userName, String courseName);

    void sendCourseChangeNotificationEmail(String toEmail, String userName, String courseName, String changeDescription);

    void sendMembershipConfirmationEmail(String toEmail, String userName, byte[] pdf, String pdfFileName);
}
