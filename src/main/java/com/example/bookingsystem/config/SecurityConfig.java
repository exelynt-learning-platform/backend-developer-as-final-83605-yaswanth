package com.example.bookingsystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.bookingsystem.security.JwtRequestFilter;
import com.example.bookingsystem.service.AccountService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AccountService accountService;

    public SecurityConfig(
            JwtRequestFilter jwtRequestFilter,
            AccountService accountService) {

        this.jwtRequestFilter = jwtRequestFilter;
        this.accountService = accountService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(accountService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authenticationProvider(
                        authenticationProvider(passwordEncoder()))

                .authorizeHttpRequests(auth -> auth

                        // Authentication
                        .requestMatchers(
                                "/auth/login"
                        ).permitAll()

                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Public resource viewing
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/assets/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // ADMIN resource management
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/assets/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/assets/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/api/assets/**"
                        ).hasRole("ADMIN")

                        // USER booking creation
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/bookings"
                        ).hasRole("USER")

                        // USER own bookings
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/bookings/my"
                        ).hasRole("USER")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/bookings/*"
                        ).hasRole("USER")

                        // ADMIN booking management
                        .requestMatchers(
                                "/api/admin/bookings/**"
                        ).hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        ErrorResponseWriter.write(
                                                response,
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Authentication required"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        ErrorResponseWriter.write(
                                                response,
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "Access denied"
                                        )
                        )
                )

                .addFilterBefore(
                        jwtRequestFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Small reusable writer for Spring Security errors.
     *
     * This keeps the 401 and 403 responses consistent and avoids
     * duplicating JSON response-writing code inside the security
     * configuration.
     */
    static final class ErrorResponseWriter {

        private static final ObjectMapper OBJECT_MAPPER =
                new ObjectMapper();

        private ErrorResponseWriter() {
        }

        static void write(
                HttpServletResponse response,
                int status,
                String message) throws IOException {

            response.setStatus(status);
            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> body =
                    new LinkedHashMap<>();

            body.put(
                    "timestamp",
                    LocalDateTime.now().toString()
            );

            body.put(
                    "status",
                    status
            );

            body.put(
                    "message",
                    message
            );

            response.getWriter().write(
                    OBJECT_MAPPER.writeValueAsString(body)
            );
        }
    }
}