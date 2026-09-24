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
import com.example.bookingsystem.util.PageableFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final PageableFactory pageableFactory;

    public BookingService(
            BookingRepository bookingRepository,
            AccountRepository accountRepository,
            AssetRepository assetRepository,
            PageableFactory pageableFactory) {

        this.bookingRepository = bookingRepository;
        this.accountRepository = accountRepository;
        this.assetRepository = assetRepository;
        this.pageableFactory = pageableFactory;
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

        /*
         * Lock the asset row before checking for conflicts.
         *
         * This prevents two concurrent booking transactions
         * from both passing the conflict check for the same asset.
         */
        Asset asset = getAssetForBooking(request.assetId());

        validateAssetAvailable(asset);

        validateNoConflict(
                asset.getId(),
                request.startAt(),
                request.endAt(),
                null);

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

        /*
         * Lock the asset before checking availability/conflicts.
         */
        Asset asset = getAssetForBooking(request.assetId());

        validateAssetAvailable(asset);

        validateNoConflict(
                asset.getId(),
                request.startAt(),
                request.endAt(),
                null);

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

        /*
         * Lock the target asset before checking for conflicts.
         *
         * The current booking is excluded from the conflict check
         * so that a booking does not conflict with itself.
         */
        Asset asset = getAssetForBooking(request.assetId());

        validateAssetAvailable(asset);

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

        validatePriceRange(minPrice, maxPrice);

        Pageable pageable = pageableFactory.create(
                page,
                size,
                sortBy,
                direction,
                "createdAt",
                "id",
                "startAt",
                "endAt",
                "price",
                "createdAt",
                "state");

        return bookingRepository.searchMine(
                        username,
                        state,
                        minPrice,
                        maxPrice,
                        pageable)
                .map(BookingResponse::from);
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

        validatePriceRange(minPrice, maxPrice);

        Pageable pageable = pageableFactory.create(
                page,
                size,
                sortBy,
                direction,
                "createdAt",
                "id",
                "startAt",
                "endAt",
                "price",
                "createdAt",
                "state");

        return bookingRepository.searchAll(
                        state,
                        minPrice,
                        maxPrice,
                        pageable)
                .map(BookingResponse::from);
    }

    // =========================================================
    // USER - VIEW OWN BOOKING BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public BookingResponse findOwn(
            Long id,
            String username) {

        Booking booking = getBooking(id);

        /*
         * Deliberately return 404 instead of 403 when the booking
         * belongs to another user. This prevents leaking whether
         * another user's booking exists.
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse changeState(
            Long id,
            BookingState newState) {

        if (newState == null) {
            throw new IllegalArgumentException(
                    "Booking state is required");
        }

        Booking booking = getBooking(id);

        BookingState currentState = booking.getState();

        validateStateTransition(
                currentState,
                newState);

        /*
         * A booking becomes an active reservation when it is
         * confirmed. Lock the asset before checking availability
         * and overlapping bookings.
         */
        if (newState == BookingState.CONFIRMED) {

            Asset asset = getAssetForBooking(
                    booking.getAsset().getId());

            validateAssetAvailable(asset);

            validateNoConflict(
                    asset.getId(),
                    booking.getStartAt(),
                    booking.getEndAt(),
                    booking.getId());
        }

        booking.setState(newState);

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    // =========================================================
    // ADMIN - DELETE BOOKING
    // =========================================================

    @Transactional
    public void remove(Long id) {

        Booking booking = getBooking(id);

        bookingRepository.delete(booking);
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

        /*
         * createdAt is automatically populated by Booking's
         * @PrePersist lifecycle callback.
         */

        return booking;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateTime(
            LocalDateTime startAt,
            LocalDateTime endAt) {

        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required");
        }

        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException(
                    "End time must be after start time");
        }
    }

    private void validatePriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        if (minPrice != null && minPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Maximum price cannot be negative");
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price");
        }
    }

    private void validateAssetAvailable(
            Asset asset) {

        if (!asset.isAvailable()) {
            throw new IllegalArgumentException(
                    "Asset is not available");
        }
    }

    // =========================================================
    // VALIDATE BOOKING CONFLICT
    // =========================================================

    private void validateNoConflict(
            Long assetId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludedBookingId) {

        long conflicts =
                bookingRepository.countConflictingBookings(
                        assetId,
                        startAt,
                        endAt,
                        BookingState.CANCELLED,
                        excludedBookingId);

        if (conflicts > 0) {
            throw new BookingConflictException(
                    "Asset is already booked for the requested time");
        }
    }

    // =========================================================
    // STATE TRANSITIONS
    // =========================================================

    private void validateStateTransition(
            BookingState currentState,
            BookingState newState) {

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

    // =========================================================
    // HELPERS
    // =========================================================

    private Asset getAsset(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Asset ID is required");
        }

        return assetRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Asset not found: " + id));
    }

    /*
     * Booking operations use a pessimistic write lock on the
     * asset row. This makes the conflict check and subsequent
     * booking save coordinate on the same asset.
     */
    private Asset getAssetForBooking(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Asset ID is required");
        }

        return assetRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Asset not found: " + id));
    }

    private Booking getBooking(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Booking ID is required");
        }

        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Booking not found: " + id));
    }
}