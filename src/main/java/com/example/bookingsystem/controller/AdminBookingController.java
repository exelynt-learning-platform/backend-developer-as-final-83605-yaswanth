package com.example.bookingsystem.controller;

import com.example.bookingsystem.dto.AdminBookingRequest;
import com.example.bookingsystem.dto.BookingFilter;
import com.example.bookingsystem.dto.BookingResponse;
import com.example.bookingsystem.dto.BookingStateRequest;
import com.example.bookingsystem.service.BookingService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public Page<BookingResponse> getAll(
            @ParameterObject
            @ModelAttribute BookingFilter filter) {

        return bookingService.all(
                filter.getState(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getPage(),
                filter.getSize(),
                filter.getSortBy(),
                filter.getDirection());
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return bookingService.findAny(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @Valid @RequestBody AdminBookingRequest request) {

        return bookingService.createByAdmin(request);
    }

    @PutMapping("/{id}")
    public BookingResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AdminBookingRequest request) {

        return bookingService.updateByAdmin(
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