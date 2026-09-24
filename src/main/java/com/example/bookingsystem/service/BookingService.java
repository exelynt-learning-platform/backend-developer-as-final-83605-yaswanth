package com.example.bookingsystem.service;

import com.example.bookingsystem.dto.AdminBookingRequest;
import com.example.bookingsystem.dto.BookingRequest;
import com.example.bookingsystem.dto.BookingResponse;
import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.entity.Asset;
import com.example.bookingsystem.entity.Booking;
import com.example.bookingsystem.entity.BookingState;
import com.example.bookingsystem.exception.BookingConflictException;
import com.example.bookingsystem.exception.NotFoundException;
import com.example.bookingsystem.repository.AccountRepository;
import com.example.bookingsystem.repository.AssetRepository;
import com.example.bookingsystem.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AccountRepository accountRepository;
    private final AssetRepository assetRepository;

    public BookingService(
            BookingRepository bookingRepository,
            AccountRepository accountRepository,
            AssetRepository assetRepository) {

        this.bookingRepository = bookingRepository;
        this.accountRepository = accountRepository;
        this.assetRepository = assetRepository;
    }

    // =========================================================
    // USER - CREATE BOOKING
    // =========================================================

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse create(
            String username,
            BookingRequest request) {

        validateTime(request.startAt(), request.endAt());

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() ->
                        new NotFoundException("Account not found"));

        Asset asset = getAsset(request.assetId());

        validateAssetAvailable(asset);

        validateNoConflict(
                asset.getId(),
                request.startAt(),
                request.endAt());

        Booking booking = buildBooking(
                account,
                asset,
                request.startAt(),
                request.endAt(),
                BookingState.PENDING);

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    // =========================================================
    // ADMIN - CREATE BOOKING
    // =========================================================

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse createByAdmin(
            AdminBookingRequest request) {

        validateTime(request.startAt(), request.endAt());

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Account not found: "
                                        + request.accountId()));

        Asset asset = getAsset(request.assetId());

        validateAssetAvailable(asset);

        validateNoConflict(
                asset.getId(),
                request.startAt(),
                request.endAt());

        Booking booking = buildBooking(
                account,
                asset,
                request.startAt(),
                request.endAt(),
                request.state());

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    // =========================================================
    // ADMIN - UPDATE BOOKING
    // =========================================================

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse updateByAdmin(
            Long id,
            AdminBookingRequest request) {

        validateTime(request.startAt(), request.endAt());

        Booking booking = getBooking(id);

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Account not found: "
                                        + request.accountId()));

        Asset asset = getAsset(request.assetId());

        validateAssetAvailable(asset);

        /*
         * Exclude the booking being updated from the conflict check.
         * Otherwise the booking would conflict with itself.
         */
        validateNoConflict(
                asset.getId(),
                request.startAt(),
                request.endAt(),
                id);

        booking.setAccount(account);
        booking.setAsset(asset);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPrice(asset.getPrice());
        booking.setState(request.state());

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    // =========================================================
    // USER - VIEW OWN BOOKINGS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<BookingResponse> mine(
            String username,
            BookingState state,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        validatePage(page, size);

        String property = switch (sortBy) {
            case "id" -> "id";
            case "startAt" -> "startAt";
            case "endAt" -> "endAt";
            case "price" -> "price";
            case "createdAt" -> "createdAt";
            case "state" -> "state";
            default -> throw new IllegalArgumentException(
                    "Invalid sortBy. Allowed values: id, startAt, endAt, price, createdAt, state");
        };

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(property).ascending()
                : Sort.by(property).descending();

        Pageable pageable = PageRequest.of(
                page,
                size,
                sort);

        return bookingRepository
                .searchMine(
                        username,
                        state,
                        minPrice,
                        maxPrice,
                        pageable)
                .map(BookingResponse::from);
    }

    // =========================================================
    // USER - VIEW ONE OWN BOOKING
    // =========================================================

    @Transactional(readOnly = true)
    public BookingResponse findOwn(
            Long bookingId,
            String username) {

        Booking booking = getBooking(bookingId);

        /*
         * Important security check:
         * USER can only see their own booking.
         */
        if (!booking.getAccount()
                .getUsername()
                .equals(username)) {

            throw new NotFoundException(
                    "Booking not found");
        }

        return BookingResponse.from(booking);
    }

    // =========================================================
    // ADMIN - VIEW ALL BOOKINGS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<BookingResponse> all(
            BookingState state,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        validatePage(page, size);

        String property = switch (sortBy) {
            case "id" -> "id";
            case "startAt" -> "startAt";
            case "endAt" -> "endAt";
            case "price" -> "price";
            case "createdAt" -> "createdAt";
            case "state" -> "state";
            default -> throw new IllegalArgumentException(
                    "Invalid sortBy. Allowed values: id, startAt, endAt, price, createdAt, state");
        };

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(property).ascending()
                : Sort.by(property).descending();

        Pageable pageable = PageRequest.of(
                page,
                size,
                sort);

        return bookingRepository
                .searchAll(
                        state,
                        minPrice,
                        maxPrice,
                        pageable)
                .map(BookingResponse::from);
    }

    // =========================================================
    // ADMIN - VIEW ANY BOOKING
    // =========================================================

    @Transactional(readOnly = true)
    public BookingResponse findAny(Long id) {

        return BookingResponse.from(
                getBooking(id));
    }

    // =========================================================
    // ADMIN - CHANGE BOOKING STATE
    // =========================================================

    @Transactional
    public BookingResponse changeState(
            Long id,
            BookingState newState) {

        Booking booking = getBooking(id);

        booking.setState(newState);

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    // =========================================================
    // ADMIN - DELETE BOOKING
    // =========================================================

    @Transactional
    public void remove(Long id) {

        bookingRepository.delete(
                getBooking(id));
    }

    // =========================================================
    // BUILD BOOKING
    // =========================================================

    private Booking buildBooking(
            Account account,
            Asset asset,
            LocalDateTime startAt,
            LocalDateTime endAt,
            BookingState state) {

        Booking booking = new Booking();

        booking.setAccount(account);
        booking.setAsset(asset);
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setPrice(asset.getPrice());
        booking.setState(state);
        booking.setCreatedAt(LocalDateTime.now());

        return booking;
    }

    // =========================================================
    // FIND BOOKING
    // =========================================================

    private Booking getBooking(Long id) {

        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Booking not found: " + id));
    }

    // =========================================================
    // FIND ASSET
    // =========================================================

    private Asset getAsset(Long id) {

        return assetRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Asset not found: " + id));
    }

    // =========================================================
    // VALIDATE BOOKING TIME
    // =========================================================

    private void validateTime(
            LocalDateTime startAt,
            LocalDateTime endAt) {

        if (!endAt.isAfter(startAt)) {

            throw new IllegalArgumentException(
                    "End time must be after start time");
        }
    }

    // =========================================================
    // VALIDATE ASSET AVAILABILITY
    // =========================================================

    private void validateAssetAvailable(
            Asset asset) {

        if (!asset.isAvailable()) {

            throw new IllegalArgumentException(
                    "Asset is currently unavailable");
        }
    }

    // =========================================================
    // VALIDATE BOOKING CONFLICT - CREATE
    // =========================================================

    private void validateNoConflict(
            Long assetId,
            LocalDateTime startAt,
            LocalDateTime endAt) {

        validateNoConflict(
                assetId,
                startAt,
                endAt,
                null);
    }

    // =========================================================
    // VALIDATE BOOKING CONFLICT
    // =========================================================

    private void validateNoConflict(
            Long assetId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludedBookingId) {

        long conflicts = bookingRepository.countConflictingBookings(
                assetId,
                BookingState.CANCELLED,
                startAt,
                endAt,
                excludedBookingId);

        if (conflicts > 0) {
            throw new BookingConflictException(
                    "Asset is already booked for the selected time");
        }
    }

    // =========================================================
    // VALIDATE PAGINATION
    // =========================================================

    private void validatePage(
            int page,
            int size) {

        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0");
        }

        if (size < 1 || size > 100) {

            throw new IllegalArgumentException(
                    "Size must be between 1 and 100");
        }
    }
}