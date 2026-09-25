package com.example.bookingsystem.dto;

import com.example.bookingsystem.entity.BookingState;
import org.springdoc.core.annotations.ParameterObject;

import java.math.BigDecimal;

@ParameterObject
public class BookingFilter {

    private BookingState state;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private int page = 0;

    private int size = 10;

    private String sortBy = "createdAt";

    private String direction = "desc";

    public BookingState getState() {
        return state;
    }

    public void setState(BookingState state) {
        this.state = state;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}