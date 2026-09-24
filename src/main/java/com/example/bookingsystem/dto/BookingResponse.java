package com.example.bookingsystem.dto;

import com.example.bookingsystem.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long accountId,
        String username,
        Long assetId,
        String assetName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        BigDecimal price,
        String state,
        LocalDateTime createdAt
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getAccount().getId(),
                booking.getAccount().getUsername(),
                booking.getAsset().getId(),
                booking.getAsset().getName(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getPrice(),
                booking.getState().name(),
                booking.getCreatedAt()
        );
    }
}