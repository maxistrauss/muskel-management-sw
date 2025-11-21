package de.oth.muskelmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice that adds common model attributes to all controllers.
 * This is used to make the current request URI available to Thymeleaf templates
 * since #request is no longer available by default in Thymeleaf 3.1+.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

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
}