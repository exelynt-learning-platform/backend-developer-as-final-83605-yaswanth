package com.example.bookingsystem.service;

import com.example.bookingsystem.entity.BookingState;
import org.springframework.stereotype.Component;

@Component
public class BookingStateMachine {

    public void validateTransition(
            BookingState currentState,
            BookingState newState) {

        if (currentState == null) {
            throw new IllegalArgumentException(
                    "Current booking state is required");
        }

        if (newState == null) {
            throw new IllegalArgumentException(
                    "New booking state is required");
        }

        if (currentState == newState) {
            return;
        }

        boolean valid = switch (currentState) {

            case PENDING ->
                    newState == BookingState.CONFIRMED
                            || newState == BookingState.CANCELLED;

            case CONFIRMED ->
                    newState == BookingState.CANCELLED;

            case CANCELLED ->
                    false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid booking state transition from "
                            + currentState
                            + " to "
                            + newState);
        }
    }
}