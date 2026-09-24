package com.example.bookingsystem.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingRequest(

        @NotNull
        Long assetId,

        @NotNull
        @Future
        LocalDateTime startAt,

        @NotNull
        LocalDateTime endAt
) {
}