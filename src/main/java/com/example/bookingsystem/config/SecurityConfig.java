package com.example.bookingsystem.config;

import com.example.bookingsystem.security.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    // =========================================================
    // AUTHENTICATION ENTRY POINT
    // =========================================================

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, exception) -> {

            response.setStatus(401);
            response.setContentType("application/json");

            response.getWriter().write(
                    "{\"timestamp\":\""
                            + LocalDateTime.now()
                            + "\",\"message\":\"Authentication is required\"}"
            );
        };
    }

    // =========================================================
    // ACCESS DENIED HANDLER
    // =========================================================

    @Bean
    AccessDeniedHandler accessDeniedHandler() {

        return (request, response, exception) -> {

            response.setStatus(403);
            response.setContentType("application/json");

            response.getWriter().write(
                    "{\"timestamp\":\""
                            + LocalDateTime.now()
                            + "\",\"message\":\"Access is denied\"}"
            );
        };
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                /*
                 * This application uses stateless JWT authentication.
                 *
                 * Authentication is performed using the Authorization
                 * header rather than browser session cookies.
                 *
                 * Therefore CSRF protection is intentionally ignored
                 * for:
                 *
                 * 1. /api/**       -> JWT protected REST API
                 * 2. /auth/login   -> JWT authentication endpoint
                 *
                 * CSRF remains enabled for any other future endpoints
                 * that might use browser/session-based authentication.
                 */
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers(
                                "/api/**",
                                "/auth/login"
                        )
                )

                /*
                 * JWT authentication is stateless.
                 *
                 * Spring Security will not create or maintain an
                 * HTTP session for authentication.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Return JSON responses for authentication and
                 * authorization failures instead of redirecting
                 * to a login page.
                 */
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint()
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler()
                                )
                )

                // =====================================================
                // AUTHORIZATION RULES
                // =====================================================

                .authorizeHttpRequests(auth -> auth

                        // -------------------------------------------------
                        // Authentication
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login"
                        )
                        .permitAll()

                        // -------------------------------------------------
                        // Swagger / OpenAPI
                        // -------------------------------------------------

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        // -------------------------------------------------
                        // Assets - USER + ADMIN can view
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/assets",
                                "/api/assets/*"
                        )
                        .hasAnyRole("USER", "ADMIN")

                        // -------------------------------------------------
                        // Assets - ADMIN only
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/assets"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/assets/*"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/assets/*"
                        )
                        .hasRole("ADMIN")

                        // -------------------------------------------------
                        // Bookings - USER creates
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/bookings"
                        )
                        .hasRole("USER")

                        // -------------------------------------------------
                        // Bookings - USER views own bookings
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/my"
                        )
                        .hasRole("USER")

                        // -------------------------------------------------
                        // Bookings - USER views individual booking
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/*"
                        )
                        .hasRole("USER")

                        // -------------------------------------------------
                        // Bookings - ADMIN management
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/admin/bookings/**"
                        )
                        .hasRole("ADMIN")

                        // -------------------------------------------------
                        // Deny everything that was not explicitly allowed
                        // -------------------------------------------------

                        .anyRequest()
                        .denyAll()
                )

                /*
                 * Process JWT authentication before Spring Security's
                 * username/password authentication filter.
                 */
                .addFilterBefore(
                        jwtRequestFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}