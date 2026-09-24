package com.example.bookingsystem.dto;

public record LoginResponse(
        String token,
        String username,
        String role
) {
}