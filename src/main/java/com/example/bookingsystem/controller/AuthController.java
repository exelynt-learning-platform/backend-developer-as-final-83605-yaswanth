package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.LoginRequest;
import com.example.bookingsystem.dto.LoginResponse;
import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.security.JwtTokenService;
import com.example.bookingsystem.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AccountService accountService;
    private final JwtTokenService tokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            AccountService accountService,
            JwtTokenService tokenService) {

        this.authenticationManager = authenticationManager;
        this.accountService = accountService;
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
                accountService.getByUsername(request.username());

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