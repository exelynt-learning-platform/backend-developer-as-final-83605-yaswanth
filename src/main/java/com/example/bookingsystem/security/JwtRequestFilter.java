package com.example.bookingsystem.security;

import com.example.bookingsystem.service.AccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtTokenService tokenService;
    private final AccountService accountService;

    public JwtRequestFilter(
            JwtTokenService tokenService,
            AccountService accountService) {

        this.tokenService = tokenService;
        this.accountService = accountService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!tokenService.valid(token)) {

            LOGGER.debug(
                    "Invalid JWT received for request: {} {}",
                    request.getMethod(),
                    request.getRequestURI()
            );

            chain.doFilter(request, response);
            return;
        }

        String username = tokenService.username(token);

        if (SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            try {

                UserDetails details =
                        accountService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                details,
                                null,
                                details.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } catch (UsernameNotFoundException ex) {

                LOGGER.debug(
                        "JWT references a non-existent account: {}",
                        username
                );

                // Token is valid but the referenced account
                // no longer exists.
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}