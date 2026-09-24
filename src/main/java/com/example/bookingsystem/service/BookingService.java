package com.example.bookingsystem.service;

import com.example.bookingsystem.dto.*;
import com.example.bookingsystem.entity.*;
import com.example.bookingsystem.exception.NotFoundException;
import com.example.bookingsystem.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
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

    @Transactional
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
                request.endAt(),
                null);

        Booking booking = new Booking();

        booking.setAccount(account);
        booking.setAsset(asset);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPrice(asset.getPrice());

        // USER bookings start as PENDING
        booking.setState(BookingState.PENDING);

        booking.setCreatedAt(LocalDateTime.now());

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    // =========================================================
    // ADMIN - CREATE BOOKING
    // =========================================================

    @Transactional
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
                request.endAt(),
                null);

        Booking booking = new Booking();

        booking.setAccount(account);
        booking.setAsset(asset);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPrice(asset.getPrice());

        // ADMIN can choose the initial state
        booking.setState(request.state());

        booking.setCreatedAt(LocalDateTime.now());

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    // =========================================================
    // ADMIN - UPDATE BOOKING
    // =========================================================

    @Transactional
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
         * Exclude the current booking from the conflict check.
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
            int size) {

        validatePage(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending());

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
            int size) {

        validatePage(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending());

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


    @Transactional
    public BookingResponse createForAdmin(
            String username,
            BookingRequest request) {

        if (!request.endAt().isAfter(request.startAt())) {
            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() ->
                        new NotFoundException("Account not found"));

        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() ->
                        new NotFoundException("Asset not found"));

        if (!asset.isAvailable()) {
            throw new IllegalArgumentException(
                    "Asset is currently unavailable");
        }

        long conflicts = bookingRepository
                .countByAssetIdAndStateNotAndStartAtLessThanAndEndAtGreaterThan(
                        asset.getId(),
                        BookingState.CANCELLED,
                        request.endAt(),
                        request.startAt());

        if (conflicts > 0) {
            throw new IllegalArgumentException(
                    "Asset is already booked for the selected time");
        }

        Booking booking = new Booking();

        booking.setAccount(account);
        booking.setAsset(asset);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPrice(asset.getPrice());
        booking.setState(BookingState.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        return BookingResponse.from(
                bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse updateForAdmin(
            Long id,
            BookingRequest request) {

        if (!request.endAt().isAfter(request.startAt())) {
            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        Booking booking = getBooking(id);

        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() ->
                        new NotFoundException("Asset not found"));

        if (!asset.isAvailable()) {
            throw new IllegalArgumentException(
                    "Asset is currently unavailable");
        }

        long conflicts = bookingRepository
                .countByAssetIdAndStateNotAndStartAtLessThanAndEndAtGreaterThan(
                        asset.getId(),
                        BookingState.CANCELLED,
                        request.endAt(),
                        request.startAt());

        if (conflicts > 0) {
            throw new IllegalArgumentException(
                    "Asset is already booked for the selected time");
        }

        booking.setAsset(asset);
        booking.setStartAt(request.startAt());
        booking.setEndAt(request.endAt());
        booking.setPrice(asset.getPrice());

        return BookingResponse.from(
                bookingRepository.save(booking));
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
    // VALIDATE BOOKING CONFLICT
    // =========================================================

    private void validateNoConflict(
            Long assetId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludedBookingId) {

        long conflicts =
                bookingRepository
                        .countByAssetIdAndStateNotAndStartAtLessThanAndEndAtGreaterThan(
                                assetId,
                                BookingState.CANCELLED,
                                endAt,
                                startAt);

        /*
         * When updating an existing booking, the repository
         * query also finds the booking being updated.
         *
         * Remove that booking from the conflict count.
         */
        if (excludedBookingId != null) {

            Booking existing =
                    bookingRepository
                            .findById(excludedBookingId)
                            .orElse(null);

            if (existing != null
                    && existing.getAsset()
                    .getId()
                    .equals(assetId)
                    && existing.getState()
                    != BookingState.CANCELLED
                    && existing.getStartAt()
                    .isBefore(endAt)
                    && existing.getEndAt()
                    .isAfter(startAt)) {

                conflicts--;
            }
        }

        if (conflicts > 0) {

            throw new IllegalArgumentException(
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