package com.example.bookingsystem.service;

import com.example.bookingsystem.dto.*;
import com.example.bookingsystem.entity.Asset;
import com.example.bookingsystem.exception.NotFoundException;
import com.example.bookingsystem.repository.AssetRepository;
import com.example.bookingsystem.util.PageableFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final PageableFactory pageableFactory;

    public AssetService(
            AssetRepository assetRepository,
            PageableFactory pageableFactory) {

        this.assetRepository = assetRepository;
        this.pageableFactory = pageableFactory;
    }

    public Page<AssetResponse> find(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String category,
            Boolean available,
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
                "id",
                "id",
                "name",
                "price",
                "category");

        return assetRepository
                .search(
                        minPrice,
                        maxPrice,
                        category,
                        available,
                        pageable)
                .map(AssetResponse::from);
    }

    public AssetResponse findOne(Long id) {
        return AssetResponse.from(getAsset(id));
    }

    public AssetResponse create(AssetRequest request) {

        Asset asset = new Asset();

        copy(request, asset);

        return AssetResponse.from(
                assetRepository.save(asset));
    }

    public AssetResponse update(
            Long id,
            AssetRequest request) {

        Asset asset = getAsset(id);

        copy(request, asset);

        return AssetResponse.from(
                assetRepository.save(asset));
    }

    public void delete(Long id) {
        assetRepository.delete(getAsset(id));
    }

    private Asset getAsset(Long id) {

        return assetRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Asset not found: " + id));
    }

    private void copy(
            AssetRequest request,
            Asset asset) {

        asset.setName(request.name());
        asset.setCategory(request.category());
        asset.setDescription(request.description());
        asset.setPrice(request.price());
        asset.setAvailable(request.available());
    }

    private void validatePriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice cannot be greater than maxPrice");
        }
    }
}