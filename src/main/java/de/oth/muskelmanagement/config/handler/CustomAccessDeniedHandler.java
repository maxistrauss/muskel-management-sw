package de.oth.muskelmanagement.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/api") || requestUri.startsWith(request.getContextPath() + "/api")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\": \"Access Denied\", \"message\": \"You do not have permission to access this resource.\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
