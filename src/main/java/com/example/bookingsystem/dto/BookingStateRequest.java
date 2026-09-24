package com.example.bookingsystem.dto;

import com.example.bookingsystem.entity.BookingState;
import jakarta.validation.constraints.NotNull;

public record BookingStateRequest(

        @NotNull
        BookingState state

) {
}