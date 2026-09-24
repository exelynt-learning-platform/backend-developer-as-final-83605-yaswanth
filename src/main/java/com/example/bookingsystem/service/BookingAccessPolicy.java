package com.example.bookingsystem.service;

import com.example.bookingsystem.entity.Booking;
import com.example.bookingsystem.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BookingAccessPolicy {

    /**
     * Verifies that the booking belongs to the authenticated user.
     *
     * A 404 is deliberately returned instead of 403 when the booking
     * belongs to another user. This prevents revealing whether a
     * booking owned by another user exists.
     */
    public void verifyOwner(
            Booking booking,
            String username) {

        if (!booking.getAccount()
                .getUsername()
                .equals(username)) {

            throw new NotFoundException(
                    "Booking not found");
        }
    }
}