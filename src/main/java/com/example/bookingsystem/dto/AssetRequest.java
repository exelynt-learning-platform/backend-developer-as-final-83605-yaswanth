package com.example.bookingsystem.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AssetRequest(

        @NotBlank
        String name,

        @NotBlank
        String category,

        @NotBlank
        String description,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal price,

        @NotNull
        Boolean available
) {
}