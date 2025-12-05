package de.oth.muskelmanagement.controller.web.auth;

import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.TwoFactorAuthService;
import de.oth.muskelmanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TwoFactorController {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorController.class);

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private UserService userService;

    @GetMapping("/verify-2fa")
    public String showVerificationPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("2FA_USER_ID");
        String email = (String) session.getAttribute("2FA_USER_EMAIL");

        if (userId == null || email == null) {
            logger.warn("No 2FA session found, redirecting to login");
            return "redirect:/login";
        }

        model.addAttribute("email", maskEmail(email));
        model.addAttribute("remainingAttempts", twoFactorAuthService.getRemainingAttempts(userId));

        return "verify-2fa";
    }

    @PostMapping("/verify-2fa")
    public String verifyCode(@RequestParam("code") String code, HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("2FA_USER_ID");

        if (userId == null) {
            logger.warn("No user ID in session for 2FA verification");
            return "redirect:/login";
        }

        boolean isValid = twoFactorAuthService.validateCode(userId, code);

        if (isValid) {
            logger.info("2FA code validated successfully for user ID: {}", userId);

            // Retrieve the authentication from session
            Authentication auth = (Authentication) session.getAttribute("2FA_AUTH");

            if (auth != null) {
                // Complete the authentication by setting it in SecurityContext
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(auth);

                // Store the security context in the session
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

                logger.info("Authentication completed for user ID: {}", userId);
            }

            // Clean up 2FA session attributes
            session.removeAttribute("2FA_USER_ID");
            session.removeAttribute("2FA_USER_EMAIL");
            session.removeAttribute("2FA_AUTH");

            redirectAttributes.addFlashAttribute("success", "2FA-Verifizierung erfolgreich!");
            return "redirect:/";
        } else {
            logger.warn("Invalid 2FA code for user ID: {}", userId);

            int remainingAttempts = twoFactorAuthService.getRemainingAttempts(userId);

            if (remainingAttempts > 0) {
                redirectAttributes.addFlashAttribute("error",
                        "Invalid code. You have " + remainingAttempts + " attempt(s) remaining.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Maximum number of attempts reached. Please request a new code.");
            }

            return "redirect:/verify-2fa";
        }
    }

    @PostMapping("/verify-2fa/resend")
    public String resendCode(HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("2FA_USER_ID");

        if (userId == null) {
            logger.warn("No user ID in session for code resend");
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail((String) session.getAttribute("2FA_USER_EMAIL"));

            if (user == null) {
                logger.error("User not found for code resend");
                return "redirect:/login?error";
            }

            twoFactorAuthService.generateAndSendCode(user);
            logger.info("New 2FA code sent to user ID: {}", userId);

            redirectAttributes.addFlashAttribute("success", "A new code has been sent to your email address.");
        } catch (Exception e) {
            logger.error("Failed to resend 2FA code", e);
            redirectAttributes.addFlashAttribute("error", "Error sending code. Please try again.");
        }

        return "redirect:/verify-2fa";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username + "@" + domain;
        }

        return username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1) + "@"
                + domain;
    }
}
