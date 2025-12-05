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
    public void sendTwoFactorCode(String toEmail, String code, String userName) {
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
    public void sendSubscriptionExpiryReminder(String toEmail, String userName, String tarifName, LocalDate expiryDate,
            long daysRemaining) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Subscription is Expiring Soon - MuskelManagement");

            String htmlContent = buildExpiryReminderEmailTemplate(userName, tarifName, expiryDate, daysRemaining);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Subscription expiry reminder email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send expiry reminder email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send expiry reminder email", e);
        }
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

    private String buildExpiryReminderEmailTemplate(String userName, String tarifName, LocalDate expiryDate,
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
                tarifName != null ? tarifName : "Your Plan", formattedDate);
    }
}
