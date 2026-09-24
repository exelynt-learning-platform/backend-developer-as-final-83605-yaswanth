package com.example.bookingsystem.config;

import com.example.bookingsystem.entity.AccessLevel;
import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.entity.Asset;
import com.example.bookingsystem.repository.AccountRepository;
import com.example.bookingsystem.repository.AssetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadInitialData(
            AccountRepository accountRepository,
            AssetRepository assetRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // =========================================================
            // ADMIN USER
            // =========================================================

            if (!accountRepository.existsByUsername("practice-admin")) {

                Account admin = new Account();

                admin.setUsername("practice-admin");
                admin.setEmail("practice-admin@example.com");
                admin.setPasswordHash(
                        passwordEncoder.encode("Admin@123"));
                admin.setAccessLevel(AccessLevel.ADMIN);
                admin.setEnabled(true);

                accountRepository.save(admin);
            }

            // =========================================================
            // USER 1
            // =========================================================

            if (!accountRepository.existsByUsername("practice-user")) {

                Account user = new Account();

                user.setUsername("practice-user");
                user.setEmail("practice-user@example.com");
                user.setPasswordHash(
                        passwordEncoder.encode("User@123"));
                user.setAccessLevel(AccessLevel.USER);
                user.setEnabled(true);

                accountRepository.save(user);
            }

            // =========================================================
            // USER 2
            // Used for reservation ownership testing
            // =========================================================

            if (!accountRepository.existsByUsername("practice-user-2")) {

                Account user2 = new Account();

                user2.setUsername("practice-user-2");
                user2.setEmail("practice-user-2@example.com");
                user2.setPasswordHash(
                        passwordEncoder.encode("User2@123"));
                user2.setAccessLevel(AccessLevel.USER);
                user2.setEnabled(true);

                accountRepository.save(user2);
            }

            // =========================================================
            // SAMPLE RESOURCES
            // =========================================================

            if (assetRepository.count() == 0) {

                // Resource 1
                Asset room = new Asset();

                room.setName("Meeting Room A");
                room.setCategory("ROOM");
                room.setDescription(
                        "Small meeting room with projector");
                room.setPrice(new BigDecimal("800.00"));
                room.setAvailable(true);

                assetRepository.save(room);

                // Resource 2
                Asset vehicle = new Asset();

                vehicle.setName("Company Car");
                vehicle.setCategory("VEHICLE");
                vehicle.setDescription(
                        "Sedan available for business travel");
                vehicle.setPrice(new BigDecimal("2500.00"));
                vehicle.setAvailable(true);

                assetRepository.save(vehicle);

                // Resource 3
                Asset laptop = new Asset();

                laptop.setName("Developer Laptop");
                laptop.setCategory("EQUIPMENT");
                laptop.setDescription(
                        "Laptop available for temporary use");
                laptop.setPrice(new BigDecimal("1200.00"));
                laptop.setAvailable(true);

                assetRepository.save(laptop);
            }
        };
    }
}