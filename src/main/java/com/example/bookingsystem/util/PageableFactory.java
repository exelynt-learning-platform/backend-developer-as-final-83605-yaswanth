package com.example.bookingsystem.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageableFactory {

    public Pageable create(
            int page,
            int size,
            String sortBy,
            String direction,
            String defaultSort,
            String... allowedSortFields) {

        validatePage(page, size);

        String property = validateSortProperty(
                sortBy,
                defaultSort,
                allowedSortFields
        );

        if (!"asc".equalsIgnoreCase(direction)
                && !"desc".equalsIgnoreCase(direction)) {

            throw new IllegalArgumentException(
                    "Sort direction must be 'asc' or 'desc'");
        }

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(property).ascending()
                : Sort.by(property).descending();

        return PageRequest.of(
                page,
                size,
                sort
        );
    }

    private String validateSortProperty(
            String sortBy,
            String defaultSort,
            String... allowedSortFields) {

        if (sortBy == null || sortBy.isBlank()) {
            return defaultSort;
        }

        for (String allowedField : allowedSortFields) {
            if (allowedField.equals(sortBy)) {
                return sortBy;
            }
        }

        throw new IllegalArgumentException(
                "Invalid sort field: " + sortBy);
    }

    private void validatePage(int page, int size) {

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