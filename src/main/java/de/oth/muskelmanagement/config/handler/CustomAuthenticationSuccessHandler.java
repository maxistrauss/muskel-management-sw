package de.oth.muskelmanagement.config.handler;

import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.service.TwoFactorAuthService;
import de.oth.muskelmanagement.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String email = authentication.getName();
        logger.info("User authenticated: {}", email);

        User user = userService.findByEmail(email);

        if (user == null) {
            logger.error("User not found after authentication: {}", email);
            response.sendRedirect("/login?error");
            return;
        }

        // Check if 2FA is enabled for this user
        if (user.isTwoFactorEnabled()) {
            logger.info("2FA enabled for user: {}. Generating code...", email);

            try {
                // Generate and send 2FA code
                twoFactorAuthService.generateAndSendCode(user);

                // Store user information in session for 2FA verification
                HttpSession session = request.getSession();
                session.setAttribute("2FA_USER_ID", user.getId());
                session.setAttribute("2FA_USER_EMAIL", user.getEmail());
                session.setAttribute("2FA_AUTH", authentication);

                // Redirect to 2FA verification page
                response.sendRedirect("/verify-2fa");
                logger.info("Redirected to 2FA verification page for user: {}", email);

            } catch (Exception e) {
                logger.error("Failed to generate 2FA code for user: {}", email, e);
                response.sendRedirect("/login?error");
            }
        } else {
            // 2FA not enabled - proceed with normal login
            logger.info("2FA not enabled for user: {}. Proceeding with normal login", email);
            response.sendRedirect("/");
        }
    }
}