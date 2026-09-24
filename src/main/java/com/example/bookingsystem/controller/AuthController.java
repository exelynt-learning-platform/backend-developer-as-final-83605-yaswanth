package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.*;
import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.repository.AccountRepository;
import com.example.bookingsystem.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final JwtTokenService tokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            AccountRepository accountRepository,
            JwtTokenService tokenService) {

        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()));

        Account account =
                accountRepository
                        .findByUsername(request.username())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated account no longer exists"));

        String token =
                tokenService.create(
                        account.getUsername(),
                        account.getAccessLevel().name());

        return new LoginResponse(
                token,
                account.getUsername(),
                account.getAccessLevel().name());
    }
}