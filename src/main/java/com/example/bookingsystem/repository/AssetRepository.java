package com.example.bookingsystem.repository;

import com.example.bookingsystem.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Query("""
        select a from Asset a
        where (:minPrice is null or a.price >= :minPrice)
          and (:maxPrice is null or a.price <= :maxPrice)
          and (:category is null or a.category = :category)
          and (:available is null or a.available = :available)
        """)
    Page<Asset> search(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("category") String category,
            @Param("available") Boolean available,
            Pageable pageable);
}