package com.example.bookingsystem.repository;

import com.example.bookingsystem.entity.Booking;
import com.example.bookingsystem.entity.BookingState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        select b from Booking b
        where (:state is null or b.state = :state)
          and (:minPrice is null or b.price >= :minPrice)
          and (:maxPrice is null or b.price <= :maxPrice)
        """)
    Page<Booking> searchAll(
            @Param("state") BookingState state,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        select b from Booking b
        where b.account.username = :username
          and (:state is null or b.state = :state)
          and (:minPrice is null or b.price >= :minPrice)
          and (:maxPrice is null or b.price <= :maxPrice)
        """)
    Page<Booking> searchMine(
            @Param("username") String username,
            @Param("state") BookingState state,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        select count(b)
        from Booking b
        where b.asset.id = :assetId
          and b.state <> :excludedState
          and b.startAt < :endAt
          and b.endAt > :startAt
          and (:excludedBookingId is null or b.id <> :excludedBookingId)
        """)
    long countConflictingBookings(
            @Param("assetId") Long assetId,
            @Param("excludedState") BookingState excludedState,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludedBookingId") Long excludedBookingId);
}