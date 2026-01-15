package de.oth.muskelmanagement.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TwoFactorAuthFilter extends OncePerRequestFilter {

    private static final List<String> ALLOWED_URLS = Arrays.asList(
            "/verify-2fa",
            "/verify-2fa/resend",
            "/logout",
            "/css/",
            "/js/",
            "/images/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false);

        boolean isUserAuthenticated = authentication != null && authentication.isAuthenticated();
        boolean is2faPending = session != null && session.getAttribute("2FA_USER_ID") != null;
        boolean isRequestAllowed = isAllowedUrl(request.getRequestURI());

        if (isUserAuthenticated && is2faPending && !isRequestAllowed) {
            response.sendRedirect(request.getContextPath() + "/verify-2fa");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedUrl(String url) {
        return ALLOWED_URLS.stream().anyMatch(url::startsWith);
    }
}
