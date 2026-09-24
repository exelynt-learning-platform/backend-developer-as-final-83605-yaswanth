package com.example.bookingsystem.dto;

import com.example.bookingsystem.entity.BookingState;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AdminBookingRequest(

        @NotNull Long accountId,

        @NotNull Long assetId,

        @NotNull @Future LocalDateTime startAt,

        @NotNull LocalDateTime endAt,

        @NotNull BookingState state
) {
}