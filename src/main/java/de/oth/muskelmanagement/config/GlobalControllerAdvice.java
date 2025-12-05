package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice that adds common model attributes to all controllers.
 * This is used to make the current request URI and user information available to Thymeleaf templates
 * since #request is no longer available by default in Thymeleaf 3.1+.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;

    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    /**
     * Adds the current request URI to the model for all controller methods.
     * This allows templates to access the current URI via ${currentUri}.
     *
     * @param request the current HTTP request
     * @return the request URI
     */
    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Adds the current user's email to the model for all controller methods.
     * This allows templates to access the current user via ${currentUserEmail}.
     *
     * @return the current user's email or null if not authenticated
     */
    @ModelAttribute("currentUserEmail")
    public String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Adds the current user's initials to the model for all controller methods.
     * This allows templates to display user initials via ${currentUserInitials}.
     *
     * @return the current user's initials (first letter of email before @)
     */
    @ModelAttribute("currentUserInitials")
    public String currentUserInitials() {
        String email = currentUserEmail();
        if (email != null && email.contains("@")) {
            String name = email.substring(0, email.indexOf("@"));
            // Get first two characters or just first if name is short
            if (name.length() >= 2) {
                return name.substring(0, 2).toUpperCase();
            } else if (name.length() == 1) {
                return name.toUpperCase();
            }
        }
        return "U"; // Default fallback
    }

    /**
     * Adds the current user object to the model for all controller methods.
     * This allows templates to access the full user data via ${currentUser}.
     *
     * @return the current User object or null if not authenticated
     */
    @ModelAttribute("currentUser")
    public User currentUser() {
        String email = currentUserEmail();
        if (email != null) {
            return userService.findByEmail(email);
        }
        return null;
    }

    /**
     * Adds the current user's full name to the model for all controller methods.
     * This allows templates to display user's full name via ${currentUserFullName}.
     *
     * @return the current user's full name (firstname + lastname)
     */
    @ModelAttribute("currentUserFullName")
    public String currentUserFullName() {
        User user = currentUser();
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return null;
    }
}