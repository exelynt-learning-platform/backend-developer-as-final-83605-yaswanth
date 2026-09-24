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
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("!prod & !test")
public class DataInitializer {

    @Value("${app.seed.admin.username:practice-admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:practice-admin@example.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.user.username:practice-user}")
    private String userUsername;

    @Value("${app.seed.user.email:practice-user@example.com}")
    private String userEmail;

    @Value("${app.seed.user.password}")
    private String userPassword;

    @Value("${app.seed.user2.username:practice-user-2}")
    private String user2Username;

    @Value("${app.seed.user2.email:practice-user-2@example.com}")
    private String user2Email;

    @Value("${app.seed.user2.password}")
    private String user2Password;

    @Bean
    CommandLineRunner loadInitialData(
            AccountRepository accountRepository,
            AssetRepository assetRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            createAdminIfMissing(
                    accountRepository,
                    passwordEncoder
            );

            createUserIfMissing(
                    accountRepository,
                    passwordEncoder
            );

            createSecondUserIfMissing(
                    accountRepository,
                    passwordEncoder
            );

            createSampleAssetsIfEmpty(
                    assetRepository
            );
        };
    }

    private void createAdminIfMissing(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {

        if (accountRepository.existsByUsername(adminUsername)) {
            return;
        }

        Account admin = new Account();

        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPasswordHash(
                passwordEncoder.encode(adminPassword)
        );
        admin.setAccessLevel(AccessLevel.ADMIN);
        admin.setEnabled(true);

        accountRepository.save(admin);
    }

    private void createUserIfMissing(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {

        if (accountRepository.existsByUsername(userUsername)) {
            return;
        }

        Account user = new Account();

        user.setUsername(userUsername);
        user.setEmail(userEmail);
        user.setPasswordHash(
                passwordEncoder.encode(userPassword)
        );
        user.setAccessLevel(AccessLevel.USER);
        user.setEnabled(true);

        accountRepository.save(user);
    }

    private void createSecondUserIfMissing(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {

        if (accountRepository.existsByUsername(user2Username)) {
            return;
        }

        Account user = new Account();

        user.setUsername(user2Username);
        user.setEmail(user2Email);
        user.setPasswordHash(
                passwordEncoder.encode(user2Password)
        );
        user.setAccessLevel(AccessLevel.USER);
        user.setEnabled(true);

        accountRepository.save(user);
    }

    private void createSampleAssetsIfEmpty(
            AssetRepository assetRepository) {

        if (assetRepository.count() > 0) {
            return;
        }

        Asset room = new Asset();

        room.setName("Meeting Room A");
        room.setCategory("ROOM");
        room.setDescription(
                "Small meeting room with projector"
        );
        room.setPrice(new BigDecimal("800.00"));
        room.setAvailable(true);

        Asset vehicle = new Asset();

        vehicle.setName("Company Car");
        vehicle.setCategory("VEHICLE");
        vehicle.setDescription(
                "Sedan available for business travel"
        );
        vehicle.setPrice(new BigDecimal("2500.00"));
        vehicle.setAvailable(true);

        Asset laptop = new Asset();

        laptop.setName("Developer Laptop");
        laptop.setCategory("EQUIPMENT");
        laptop.setDescription(
                "Laptop available for temporary use"
        );
        laptop.setPrice(new BigDecimal("1200.00"));
        laptop.setAvailable(true);

        assetRepository.save(room);
        assetRepository.save(vehicle);
        assetRepository.save(laptop);
    }
}