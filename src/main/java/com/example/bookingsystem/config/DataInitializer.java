package com.example.bookingsystem.config;

import com.example.bookingsystem.entity.AccessLevel;
import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.entity.Asset;
import com.example.bookingsystem.repository.AccountRepository;
import com.example.bookingsystem.repository.AssetRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("seed")
public class DataInitializer {

    private final AccountRepository accountRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AccountRepository accountRepository,
            AssetRepository assetRepository,
            PasswordEncoder passwordEncoder) {

        this.accountRepository = accountRepository;
        this.assetRepository = assetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public org.springframework.boot.CommandLineRunner seedData() {

        return args -> {

            validatePassword(
                    "SEED_ADMIN_PASSWORD",
                    System.getenv("SEED_ADMIN_PASSWORD")
            );

            validatePassword(
                    "SEED_USER_PASSWORD",
                    System.getenv("SEED_USER_PASSWORD")
            );

            validatePassword(
                    "SEED_USER2_PASSWORD",
                    System.getenv("SEED_USER2_PASSWORD")
            );

            createAccounts();
            createAssets();
        };
    }

    private void createAccounts() {

        createAccount(
                System.getenv("SEED_ADMIN_USERNAME"),
                System.getenv("SEED_ADMIN_EMAIL"),
                System.getenv("SEED_ADMIN_PASSWORD"),
                AccessLevel.ADMIN
        );

        createAccount(
                System.getenv("SEED_USER_USERNAME"),
                System.getenv("SEED_USER_EMAIL"),
                System.getenv("SEED_USER_PASSWORD"),
                AccessLevel.USER
        );

        createAccount(
                System.getenv("SEED_USER2_USERNAME"),
                System.getenv("SEED_USER2_EMAIL"),
                System.getenv("SEED_USER2_PASSWORD"),
                AccessLevel.USER
        );
    }

    private void createAccount(
            String username,
            String email,
            String password,
            AccessLevel accessLevel) {

        if (username == null || username.isBlank()) {
            throw new IllegalStateException(
                    "Seed username is required"
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "Seed email is required for " + username
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Seed password is required for " + username
            );
        }

        if (accountRepository.findByUsername(username).isPresent()) {
            return;
        }

        Account account = new Account();

        account.setUsername(username);
        account.setEmail(email);
        account.setPasswordHash(
                passwordEncoder.encode(password)
        );
        account.setAccessLevel(accessLevel);
        account.setEnabled(true);

        accountRepository.save(account);
    }

    private void createAssets() {

        if (assetRepository.count() > 0) {
            return;
        }

        Asset meetingRoom = new Asset();

        meetingRoom.setName("Conference Room A");
        meetingRoom.setCategory("ROOM");
        meetingRoom.setDescription(
                "A medium-sized conference room suitable for meetings."
        );
        meetingRoom.setPrice(new BigDecimal("500.00"));
        meetingRoom.setAvailable(true);

        assetRepository.save(meetingRoom);

        Asset trainingRoom = new Asset();

        trainingRoom.setName("Training Room");
        trainingRoom.setCategory("ROOM");
        trainingRoom.setDescription(
                "Training room suitable for workshops and team sessions."
        );
        trainingRoom.setPrice(new BigDecimal("800.00"));
        trainingRoom.setAvailable(true);

        assetRepository.save(trainingRoom);

        Asset laptop = new Asset();

        laptop.setName("Development Laptop");
        laptop.setCategory("EQUIPMENT");
        laptop.setDescription(
                "Laptop available for development and testing purposes."
        );
        laptop.setPrice(new BigDecimal("1200.00"));
        laptop.setAvailable(true);

        assetRepository.save(laptop);
    }

    private void validatePassword(
            String environmentVariable,
            String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    environmentVariable + " must be configured"
            );
        }

        if (password.length() < 12) {
            throw new IllegalStateException(
                    environmentVariable
                            + " must contain at least 12 characters"
            );
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalStateException(
                    environmentVariable
                            + " must contain at least one uppercase letter"
            );
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalStateException(
                    environmentVariable
                            + " must contain at least one lowercase letter"
            );
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalStateException(
                    environmentVariable
                            + " must contain at least one digit"
            );
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            throw new IllegalStateException(
                    environmentVariable
                            + " must contain at least one special character"
            );
        }
    }
}