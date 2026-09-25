package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.BookingFilter;
import com.example.bookingsystem.dto.BookingRequest;
import com.example.bookingsystem.dto.BookingResponse;
import com.example.bookingsystem.service.BookingService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
            @ParameterObject
            @ModelAttribute BookingFilter filter) {

        return bookingService.mine(
                authentication.getName(),
                filter.getState(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getPage(),
                filter.getSize(),
                filter.getSortBy(),
                filter.getDirection());
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