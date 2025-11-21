package de.oth.muskelmanagement.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
            helper.setSubject("Ihr 2FA Verifizierungscode - MuskelManagement");

            String htmlContent = buildEmailTemplate(code, userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("2FA code email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send 2FA email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildEmailTemplate(String code, String userName) {
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
}