package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Override
    public void sendTwoFactorCodeEmail(String toEmail, String code, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your 2FA Verification Code - MuskelManagement");

            String htmlContent = buildTwoFactorEmailTemplate(code, userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("2FA code email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send 2FA email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendAccountDeactivationEmail(String toEmail, String userName, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Account Has Been Deactivated - MuskelManagement");

            String htmlContent = buildDeactivationEmailTemplate(userName, reason);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Account deactivation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send deactivation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send deactivation email", e);
        }
    }

    @Override
    public void sendAccountActivationEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Account Has Been Activated - MuskelManagement");

            String htmlContent = buildActivationEmailTemplate(userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Account activation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send activation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send activation email", e);
        }
    }

    @Override
    public void sendSubscriptionExpiryReminder(String toEmail, String userName, String pricingName,
            LocalDate expiryDate,
            long daysRemaining) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Subscription is Expiring Soon - MuskelManagement");

            String htmlContent = buildExpiryReminderEmailTemplate(userName, pricingName, expiryDate, daysRemaining);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Subscription expiry reminder email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send expiry reminder email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send expiry reminder email", e);
        }
    }

    @Override
    public void sendRegistrationConfirmationEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Registration Successful - MuskelManagement");

            String htmlContent = buildRegistrationConfirmationEmailTemplate(userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Registration confirmation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send registration confirmation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send registration confirmation email", e);
        }
    }

    private String buildRegistrationConfirmationEmailTemplate(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #10b981;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .success-box {
                            background-color: white;
                            border-left: 4px solid #10b981;
                            border-radius: 5px;
                            padding: 20px;
                            margin: 20px 0;
                            text-align: center;
                        }
                        .success-box h2 {
                            color: #10b981;
                            margin-top: 0;
                        }
                        .cta-button {
                            display: inline-block;
                            background-color: #3b82f6;
                            color: white;
                            padding: 15px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            font-weight: bold;
                            margin: 20px 0;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                            color: #6b7280;
                            font-size: 12px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Welcome!</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                
                            <div class="success-box">
                                <h2>Registration Successful!</h2>
                                <p>Welcome to the MuskelManagement community.</p>
                            </div>
                
                            <p>Your account has been successfully created. You can now log in and start your fitness journey.</p>
                
                            <div style="text-align: center;">
                                <a href="http://localhost:8080/login" class="cta-button">
                                    Login Now
                                </a>
                            </div>
                
                            <p>We are excited to have you on board!</p>
                
                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "User");
    }

    @Override
    public void sendPasswordChangeConfirmationEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Security Alert: Password Changed - MuskelManagement");

            String htmlContent = buildPasswordChangeConfirmationEmailTemplate(userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password change confirmation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password change confirmation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password change confirmation email", e);
        }
    }

    private String buildPasswordChangeConfirmationEmailTemplate(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #3b82f6;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .alert-box {
                            background-color: white;
                            border-left: 4px solid #3b82f6;
                            border-radius: 5px;
                            padding: 20px;
                            margin: 20px 0;
                        }
                        .warning {
                            background-color: #fef3c7;
                            border-left: 4px solid #f59e0b;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 3px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                            color: #6b7280;
                            font-size: 12px;
                        }
                        strong {
                            color: #1f2937;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Security Alert</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                
                            <div class="alert-box">
                                <p><strong>Your password was successfully changed.</strong></p>
                                <p>This is a confirmation that your account password has been updated.</p>
                            </div>
                
                            <div class="warning">
                                <strong>⚠️ Didn't change your password?</strong><br/>
                                If you did not make this change, please contact our support team immediately 
                                and secure your account.
                            </div>
                
                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "User");
    }

    private String buildTwoFactorEmailTemplate(String code, String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #3b82f6;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .code-box {
                            background-color: white;
                            border: 2px dashed #3b82f6;
                            border-radius: 5px;
                            padding: 20px;
                            text-align: center;
                            margin: 20px 0;
                        }
                        .code {
                            font-size: 36px;
                            font-weight: bold;
                            letter-spacing: 10px;
                            color: #3b82f6;
                            font-family: 'Courier New', monospace;
                        }
                        .warning {
                            background-color: #fef3c7;
                            border-left: 4px solid #f59e0b;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 3px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                            color: #6b7280;
                            font-size: 12px;
                        }
                        ul {
                            margin: 15px 0;
                            padding-left: 20px;
                        }
                        li {
                            margin: 8px 0;
                        }
                        strong {
                            color: #1f2937;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Two-Factor Authentication</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                
                            <p>You have requested a login to MuskelManagement.
                               Here is your verification code:</p>
                
                            <div class="code-box">
                                <div class="code">%s</div>
                            </div>
                
                            <p><strong>Important Information:</strong></p>
                            <ul>
                                <li>This code is valid for <strong>5 minutes</strong> only</li>
                                <li>You have a maximum of <strong>3 attempts</strong> to enter it</li>
                                <li>Never share this code with anyone</li>
                            </ul>
                
                            <div class="warning">
                                <strong>⚠️ Warning:</strong> If you did not request this login,
                                ignore this email and change your password immediately.
                            </div>
                
                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "User", code);
    }

    private String buildDeactivationEmailTemplate(String userName, String reason) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #ef4444;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .reason-box {
                            background-color: white;
                            border-left: 4px solid #ef4444;
                            border-radius: 5px;
                            padding: 20px;
                            margin: 20px 0;
                        }
                        .reason-box h3 {
                            margin-top: 0;
                            color: #ef4444;
                        }
                        .info {
                            background-color: #dbeafe;
                            border-left: 4px solid #3b82f6;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 3px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                            color: #6b7280;
                            font-size: 12px;
                        }
                        strong {
                            color: #1f2937;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Account Deactivation</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                
                            <p>Your account at MuskelManagement has been deactivated by an administrator.</p>
                
                            <div class="reason-box">
                                <h3>Reason for Deactivation:</h3>
                                <p>%s</p>
                            </div>
                
                            <div class="info">
                                <strong>ℹ️ What does this mean?</strong><br/>
                                You can no longer log in to your account.
                                If you have questions about the deactivation or believe this is an error,
                                please contact our support team.
                            </div>
                
                            <p>If you have any questions or concerns, please feel free to contact us at any time.</p>
                
                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "User", reason != null ? reason : "No reason provided");
    }

    private String buildActivationEmailTemplate(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #10b981;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .success-box {
                            background-color: white;
                            border-left: 4px solid #10b981;
                            border-radius: 5px;
                            padding: 20px;
                            margin: 20px 0;
                            text-align: center;
                        }
                        .success-box h2 {
                            color: #10b981;
                            margin-top: 0;
                        }
                        .info {
                            background-color: #dbeafe;
                            border-left: 4px solid #3b82f6;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 3px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                            color: #6b7280;
                            font-size: 12px;
                        }
                        strong {
                            color: #1f2937;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Account Activation</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                
                            <div class="success-box">
                                <h2>✓ Your Account Has Been Activated!</h2>
                                <p>You can now log in to your account again.</p>
                            </div>
                
                            <div class="info">
                                <strong>ℹ️ Next Steps:</strong><br/>
                                You can now log in again with your usual credentials
                                and use all features of MuskelManagement.
                            </div>
                
                            <p>Welcome back! We are pleased to have you with us again.</p>
                
                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "User");
    }

    private String buildExpiryReminderEmailTemplate(String userName, String pricingName, LocalDate expiryDate,
            long daysRemaining) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDate = expiryDate.format(formatter);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #f59e0b;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .expiry-box {
                            background-color: white;
                            border-left: 4px solid #f59e0b;
                            border-radius: 5px;
                            padding: 20px;
                            margin: 20px 0;
                            text-align: center;
                        }
                        .expiry-box h2 {
                            color: #f59e0b;
                            margin-top: 0;
                            font-size: 28px;
                        }
                        .days-remaining {
                            font-size: 48px;
                            font-weight: bold;
                            color: #ef4444;
                            margin: 10px 0;
                        }
                        .cta-button {
                            display: inline-block;
                            background-color: #3b82f6;
                            color: white;
                            padding: 15px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            font-weight: bold;
                            margin: 20px 0;
                        }
                        .info {
                            background-color: #dbeafe;
                            border-left: 4px solid #3b82f6;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 3px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 20px;
                            color: #6b7280;
                            font-size: 12px;
                        }
                        strong {
                            color: #1f2937;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Subscription Expiry Reminder</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                
                            <p>This is a friendly reminder that your subscription is expiring soon!</p>
                
                            <div class="expiry-box">
                                <h2>⏰ Time Running Out</h2>
                                <div class="days-remaining">%d</div>
                                <p style="margin: 0; font-size: 18px;">days remaining</p>
                            </div>
                
                            <div class="info">
                                <strong>📋 Subscription Details:</strong><br/>
                                <strong>Plan:</strong> %s<br/>
                                <strong>Expiry Date:</strong> %s
                            </div>
                
                            <p style="text-align: center;">
                                <strong>Don't miss out on your fitness journey!</strong><br/>
                                Renew your subscription today to continue enjoying all the benefits.
                            </p>
                
                            <div style="text-align: center;">
                                <a href="http://localhost:8080/member/tarifs" class="cta-button">
                                    Renew Subscription Now
                                </a>
                            </div>
                
                            <p style="margin-top: 30px;">If you have any questions or need assistance, please don't hesitate to contact us.</p>
                
                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "Member", daysRemaining,
                pricingName != null ? pricingName : "Your Plan", formattedDate);
    }

    @Override
    public void sendCourseEnrollmentConfirmationEmail(String toEmail, String userName, String courseName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Course Enrollment Confirmed - MuskelManagement");

            String htmlContent = buildCourseEnrollmentConfirmationTemplate(userName, courseName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Course enrollment confirmation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send course enrollment confirmation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send course enrollment confirmation email", e);
        }
    }

    @Override
    public void sendCourseEnrollmentCancelledEmail(String toEmail, String userName, String courseName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Course Enrollment Cancelled - MuskelManagement");

            String htmlContent = buildCourseEnrollmentCancelledTemplate(userName, courseName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Course enrollment cancelled email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send course enrollment cancelled email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send course enrollment cancelled email", e);
        }
    }

    @Override
    public void sendCourseWaitlistConfirmationEmail(String toEmail, String userName, String courseName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Added to Waitlist - MuskelManagement");

            String htmlContent = buildCourseWaitlistConfirmationTemplate(userName, courseName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Course waitlist confirmation email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send course waitlist confirmation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send course waitlist confirmation email", e);
        }
    }

    @Override
    public void sendCourseWaitlistPromotionEmail(String toEmail, String userName, String courseName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Spot Available - Course Enrollment Confirmed - MuskelManagement");

            String htmlContent = buildCourseWaitlistPromotionTemplate(userName, courseName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Course waitlist promotion email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send course waitlist promotion email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send course waitlist promotion email", e);
        }
    }

    @Override
    public void sendCourseChangeNotificationEmail(String toEmail, String userName, String courseName, String changeDescription) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Course Update - MuskelManagement");

            String htmlContent = buildCourseChangeNotificationTemplate(userName, courseName, changeDescription);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Course change notification email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send course change notification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send course change notification email", e);
        }
    }

    private String buildCourseEnrollmentConfirmationTemplate(String userName, String courseName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        .container {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #2563eb;
                            color: white;
                            padding: 20px;
                            border-radius: 5px 5px 0 0;
                            text-align: center;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 20px;
                            border: 1px solid #e5e7eb;
                        }
                        .success-box {
                            background-color: #d1fae5;
                            border-left: 4px solid #10b981;
                            padding: 15px;
                            margin: 15px 0;
                        }
                        .info {
                            background-color: #dbeafe;
                            border-left: 4px solid #3b82f6;
                            padding: 15px;
                            margin: 15px 0;
                        }
                        .footer {
                            background-color: #2563eb;
                            color: white;
                            padding: 20px;
                            border-radius: 0 0 5px 5px;
                            text-align: center;
                            font-size: 12px;
                        }
                        .cta-button {
                            display: inline-block;
                            background-color: #2563eb;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin-top: 10px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Enrollment Confirmation</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>Great news! You have successfully enrolled in a course.</p>

                            <div class="success-box">
                                <h2 style="margin-top: 0;">✅ Enrollment Confirmed</h2>
                                <p><strong>Course:</strong> %s</p>
                                <p style="margin-bottom: 0;">You are now a confirmed member of this course.</p>
                            </div>

                            <div class="info">
                                <strong>📌 What's Next?</strong><br/>
                                You will receive further information about the course schedule, location, and any updates directly to this email address. Make sure to mark our emails as important so you don't miss any updates.
                            </div>

                            <p style="text-align: center;">
                                <a href="http://localhost:8080/member/course-info/my" class="cta-button">
                                    View My Courses
                                </a>
                            </p>

                            <p>If you have any questions, please feel free to contact your trainer or support team.</p>

                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "Member", courseName != null ? courseName : "Your Course");
    }

    private String buildCourseEnrollmentCancelledTemplate(String userName, String courseName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        .container {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #ef4444;
                            color: white;
                            padding: 20px;
                            border-radius: 5px 5px 0 0;
                            text-align: center;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 20px;
                            border: 1px solid #e5e7eb;
                        }
                        .info-box {
                            background-color: #fee2e2;
                            border-left: 4px solid #ef4444;
                            padding: 15px;
                            margin: 15px 0;
                        }
                        .footer {
                            background-color: #ef4444;
                            color: white;
                            padding: 20px;
                            border-radius: 0 0 5px 5px;
                            text-align: center;
                            font-size: 12px;
                        }
                        .cta-button {
                            display: inline-block;
                            background-color: #2563eb;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin-top: 10px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Enrollment Cancelled</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>Your enrollment in the following course has been cancelled:</p>

                            <div class="info-box">
                                <h2 style="margin-top: 0;">❌ Enrollment Cancelled</h2>
                                <p><strong>Course:</strong> %s</p>
                                <p style="margin-bottom: 0;">You have been removed from this course.</p>
                            </div>

                            <p>If this was done by mistake or if you have questions, please contact the support team.</p>

                            <p style="text-align: center;">
                                <a href="http://localhost:8080/member/course-info/overview" class="cta-button">
                                    Browse Other Courses
                                </a>
                            </p>

                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "Member", courseName != null ? courseName : "Your Course");
    }

    private String buildCourseWaitlistConfirmationTemplate(String userName, String courseName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        .container {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #f59e0b;
                            color: white;
                            padding: 20px;
                            border-radius: 5px 5px 0 0;
                            text-align: center;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 20px;
                            border: 1px solid #e5e7eb;
                        }
                        .waitlist-box {
                            background-color: #fef3c7;
                            border-left: 4px solid #f59e0b;
                            padding: 15px;
                            margin: 15px 0;
                        }
                        .footer {
                            background-color: #f59e0b;
                            color: white;
                            padding: 20px;
                            border-radius: 0 0 5px 5px;
                            text-align: center;
                            font-size: 12px;
                        }
                        .cta-button {
                            display: inline-block;
                            background-color: #f59e0b;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin-top: 10px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Added to Waitlist</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>You have been added to the waitlist for the following course:</p>

                            <div class="waitlist-box">
                                <h2 style="margin-top: 0;">⏳ Waitlist Confirmation</h2>
                                <p><strong>Course:</strong> %s</p>
                                <p style="margin-bottom: 0;">We will notify you automatically as soon as a spot becomes available!</p>
                            </div>

                            <p>The course is currently at full capacity, but we'll get you in as soon as someone cancels their enrollment.</p>

                            <p style="text-align: center;">
                                <a href="http://localhost:8080/member/course-info/my" class="cta-button">
                                    View My Waitlist
                                </a>
                            </p>

                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "Member", courseName != null ? courseName : "Your Course");
    }

    private String buildCourseWaitlistPromotionTemplate(String userName, String courseName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        .container {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #10b981;
                            color: white;
                            padding: 20px;
                            border-radius: 5px 5px 0 0;
                            text-align: center;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 20px;
                            border: 1px solid #e5e7eb;
                        }
                        .promotion-box {
                            background-color: #d1fae5;
                            border-left: 4px solid #10b981;
                            padding: 15px;
                            margin: 15px 0;
                        }
                        .footer {
                            background-color: #10b981;
                            color: white;
                            padding: 20px;
                            border-radius: 0 0 5px 5px;
                            text-align: center;
                            font-size: 12px;
                        }
                        .cta-button {
                            display: inline-block;
                            background-color: #10b981;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin-top: 10px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Great News!</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>Exciting news! A spot has opened up in your waitlisted course.</p>

                            <div class="promotion-box">
                                <h2 style="margin-top: 0;">🎉 Enrollment Confirmed!</h2>
                                <p><strong>Course:</strong> %s</p>
                                <p style="margin-bottom: 0;">You have been automatically promoted from the waitlist and are now a confirmed member of this course.</p>
                            </div>

                            <p>Congratulations! You're now enrolled in the course. Check your account for course details, schedule, and location information.</p>

                            <p style="text-align: center;">
                                <a href="http://localhost:8080/member/course-info/my" class="cta-button">
                                    View My Courses
                                </a>
                            </p>

                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "Member", courseName != null ? courseName : "Your Course");
    }

    private String buildCourseChangeNotificationTemplate(String userName, String courseName, String changeDescription) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        .container {
                            font-family: Arial, sans-serif;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #2563eb;
                            color: white;
                            padding: 20px;
                            border-radius: 5px 5px 0 0;
                            text-align: center;
                        }
                        .content {
                            background-color: #f9fafb;
                            padding: 20px;
                            border: 1px solid #e5e7eb;
                        }
                        .update-box {
                            background-color: #dbeafe;
                            border-left: 4px solid #3b82f6;
                            padding: 15px;
                            margin: 15px 0;
                            white-space: pre-wrap;
                            word-wrap: break-word;
                        }
                        .footer {
                            background-color: #2563eb;
                            color: white;
                            padding: 20px;
                            border-radius: 0 0 5px 5px;
                            text-align: center;
                            font-size: 12px;
                        }
                        .cta-button {
                            display: inline-block;
                            background-color: #2563eb;
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin-top: 10px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>MuskelManagement</h1>
                            <p style="margin: 5px 0 0 0;">Course Update</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>There is an important update regarding one of your courses:</p>

                            <div class="update-box">
                                <h2 style="margin-top: 0;">📢 Course: %s</h2>
                                <p><strong>Update Details:</strong></p>
                                <p>%s</p>
                            </div>

                            <p>Please check your account for more details and make any necessary adjustments to your schedule.</p>

                            <p style="text-align: center;">
                                <a href="http://localhost:8080/member/course-info/my" class="cta-button">
                                    View Course Details
                                </a>
                            </p>

                            <p>If you have any questions or concerns, please don't hesitate to contact your trainer or support team.</p>

                            <p>Best regards,<br/>
                               Your MuskelManagement Team</p>
                        </div>
                        <div class="footer">
                            <p>This email was generated automatically. Please do not reply to it.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName != null ? userName : "Member", 
                            courseName != null ? courseName : "Your Course",
                            changeDescription != null ? changeDescription : "The course has been updated. Please review the details.");
    }
}