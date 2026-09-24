package com.example.bookingsystem.config;

import com.example.bookingsystem.entity.AccessLevel;
import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.entity.Asset;
import com.example.bookingsystem.repository.AccountRepository;
import com.example.bookingsystem.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Value("${app.seed.admin.username:practice-admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:practice-admin@example.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.seed.user.username:practice-user}")
    private String userUsername;

    @Value("${app.seed.user.email:practice-user@example.com}")
    private String userEmail;

    @Value("${app.seed.user.password:User@123}")
    private String userPassword;

    @Value("${app.seed.user2.username:practice-user-2}")
    private String user2Username;

    @Value("${app.seed.user2.email:practice-user-2@example.com}")
    private String user2Email;

    @Value("${app.seed.user2.password:User2@123}")
    private String user2Password;

    @Bean
    CommandLineRunner loadInitialData(
            AccountRepository accountRepository,
            AssetRepository assetRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // =========================================================
            // ADMIN USER
            // =========================================================

            if (!accountRepository.existsByUsername(adminUsername)) {

                Account admin = new Account();

                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPasswordHash(
                        passwordEncoder.encode(adminPassword));
                admin.setAccessLevel(AccessLevel.ADMIN);
                admin.setEnabled(true);

                accountRepository.save(admin);
            }

            // =========================================================
            // USER 1
            // =========================================================

            if (!accountRepository.existsByUsername(userUsername)) {

                Account user = new Account();

                user.setUsername(userUsername);
                user.setEmail(userEmail);
                user.setPasswordHash(
                        passwordEncoder.encode(userPassword));
                user.setAccessLevel(AccessLevel.USER);
                user.setEnabled(true);

                accountRepository.save(user);
            }

            // =========================================================
            // USER 2
            // Used for reservation ownership testing
            // =========================================================

            if (!accountRepository.existsByUsername(user2Username)) {

                Account user2 = new Account();

                user2.setUsername(user2Username);
                user2.setEmail(user2Email);
                user2.setPasswordHash(
                        passwordEncoder.encode(user2Password));
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