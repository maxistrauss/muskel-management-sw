package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.config.handler.CustomAccessDeniedHandler;
import de.oth.muskelmanagement.config.handler.CustomAuthenticationFailureHandler;
import de.oth.muskelmanagement.model.Role;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private de.oth.muskelmanagement.config.handler.CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Exercise API sync endpoints (ADMIN only)
                        .requestMatchers("/api/exercises/sync/**").hasRole("ADMIN")
                        .requestMatchers("/api/exercises/sync").hasRole("ADMIN")
                        .requestMatchers("/api/exercises/stats").hasRole("ADMIN")
                        
                        // Course-Exercise management (ADMIN and TRAINER only can add exercises to courses)
                        .requestMatchers("/api/exercises/courses/**").hasAnyRole("ADMIN", "TRAINER")
                        
                        // Trainer endpoints
                        .requestMatchers("/trainer/**").hasRole("TRAINER")
                        
                        // Member endpoints (includes courses and exercises)
                        // All users with ROLE_MEMBER (which includes TRAINER and ADMIN) can access
                        .requestMatchers("/member/**").hasRole("MEMBER")
                        
                        // API endpoints for courses and exercises (for AJAX calls)
                        .requestMatchers("/api/courses", "/api/courses/**").hasRole("MEMBER")
                        .requestMatchers("/api/exercises", "/api/exercises/*", "/api/exercises/filters").hasRole("MEMBER")
                        
                        // Public endpoints (no authentication required)
                        .requestMatchers("/login", "/register", "/verify-2fa", "/verify-2fa/resend",
                                        "/css/**", "/js/**", "/images/**", "/h2-console/**",
                                        "/exercise-image").permitAll()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .usernameParameter("email")
                        .permitAll())
                .httpBasic(basic -> {})  // Enable HTTP Basic Auth for API endpoints (useful for Postman/API testing)
                .logout(LogoutConfigurer::permitAll)
                .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(accessDeniedHandler))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))  // Disable CSRF for API endpoints
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return email -> {
            User user = userService.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                    user.isEnabled(), true, true, true, getAuthorities(user.getRoles()));
        };
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }
}
