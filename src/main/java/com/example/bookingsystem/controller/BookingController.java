package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.BookingRequest;
import com.example.bookingsystem.dto.BookingResponse;
import com.example.bookingsystem.entity.BookingState;
import com.example.bookingsystem.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            Authentication authentication,
            @Valid @RequestBody BookingRequest request) {

        return bookingService.create(
                authentication.getName(),
                request);
    }

    @GetMapping("/my")
    public Page<BookingResponse> myBookings(
            Authentication authentication,
            @RequestParam(required = false) BookingState state,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return bookingService.mine(
                authentication.getName(),
                state,
                minPrice,
                maxPrice,
                page,
                size,
                sortBy,
                direction);
    }

    @GetMapping("/{id}")
    public BookingResponse getOwn(
            Authentication authentication,
            @PathVariable Long id) {

        return bookingService.findOwn(
                id,
                authentication.getName());
    }
}