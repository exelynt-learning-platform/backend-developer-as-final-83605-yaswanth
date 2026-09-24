package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.BookingRequest;
import com.example.bookingsystem.dto.BookingResponse;
import com.example.bookingsystem.dto.BookingStateRequest;
import com.example.bookingsystem.entity.BookingState;
import com.example.bookingsystem.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public Page<BookingResponse> getAll(
            @RequestParam(required = false) BookingState state,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return bookingService.all(
                state,
                minPrice,
                maxPrice,
                page,
                size);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return bookingService.findAny(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @RequestParam String username,
            @Valid @RequestBody BookingRequest request) {

        return bookingService.createForAdmin(
                username,
                request);
    }

    @PutMapping("/{id}")
    public BookingResponse update(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request) {

        return bookingService.updateForAdmin(
                id,
                request);
    }

    @PatchMapping("/{id}/state")
    public BookingResponse updateState(
            @PathVariable Long id,
            @Valid @RequestBody BookingStateRequest request) {

        return bookingService.changeState(
                id,
                request.state());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookingService.remove(id);
    }
}