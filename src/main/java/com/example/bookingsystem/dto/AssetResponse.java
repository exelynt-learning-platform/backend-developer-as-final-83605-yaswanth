package com.example.bookingsystem.dto;

import com.example.bookingsystem.entity.Asset;

import java.math.BigDecimal;

public record AssetResponse(
        Long id,
        String name,
        String category,
        String description,
        BigDecimal price,
        boolean available
) {

    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getCategory(),
                asset.getDescription(),
                asset.getPrice(),
                asset.isAvailable()
        );
    }
}