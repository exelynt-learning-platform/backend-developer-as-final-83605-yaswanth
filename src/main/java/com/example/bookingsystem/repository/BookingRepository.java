package com.example.bookingsystem.repository;

import com.example.bookingsystem.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
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

    long countByAssetIdAndStateNotAndStartAtLessThanAndEndAtGreaterThan(
            Long assetId,
            BookingState excludedState,
            LocalDateTime endAt,
            LocalDateTime startAt);
}