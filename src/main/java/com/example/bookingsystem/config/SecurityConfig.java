package com.example.bookingsystem.config;

import com.example.bookingsystem.security.JwtRequestFilter;
import com.example.bookingsystem.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AccountService accountService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(accountService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    /**
     * Handles requests that do not contain valid authentication.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {

            response.setStatus(401);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\"}"
            );
        };
    }

    /**
     * Handles authenticated users who do not have
     * sufficient permissions for the requested resource.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {

            response.setStatus(403);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\"}"
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // JWT APIs are stateless, so CSRF protection is not required.
                .csrf(csrf -> csrf.disable())

                // Do not create or use HTTP sessions.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                // Custom 401 and 403 responses.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                authenticationEntryPoint())
                        .accessDeniedHandler(
                                accessDeniedHandler()))

                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // PUBLIC ENDPOINTS
                        // =====================================================

                        .requestMatchers(
                                "/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        // =====================================================
                        // RESOURCE ENDPOINTS
                        // =====================================================

                        // USER + ADMIN can view resources.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/assets/**"
                        )
                        .hasAnyRole("USER", "ADMIN")

                        // Only ADMIN can create resources.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/assets/**"
                        )
                        .hasRole("ADMIN")

                        // Only ADMIN can update resources.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/assets/**"
                        )
                        .hasRole("ADMIN")

                        // Only ADMIN can delete resources.
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/assets/**"
                        )
                        .hasRole("ADMIN")

                        // =====================================================
                        // USER BOOKING ENDPOINTS
                        // =====================================================

                        // USER can create their own booking.
                        // The username comes from JWT authentication,
                        // not from the request body.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/bookings"
                        )
                        .hasRole("USER")

                        // USER can view only their own bookings.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/my"
                        )
                        .hasRole("USER")

                        // USER can access an individual booking.
                        // BookingService performs the ownership check.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/*"
                        )
                        .hasRole("USER")

                        // =====================================================
                        // ADMIN BOOKING ENDPOINTS
                        // =====================================================

                        // ADMIN has full booking-management access.
                        .requestMatchers(
                                "/api/admin/bookings/**"
                        )
                        .hasRole("ADMIN")

                        // =====================================================
                        // DEFAULT
                        // =====================================================

                        // Any endpoint not explicitly declared above
                        // requires an authenticated user.
                        .anyRequest()
                        .authenticated()
                )

                // Read and validate JWT before Spring Security
                // performs authorization.
                .addFilterBefore(
                        jwtRequestFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}