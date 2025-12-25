package com.ecommerce.userservice.config;

import com.ecommerce.userservice.entity.Role;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.repository.RoleRepository;
import com.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Создание ролей
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> {
                    log.info("Creating ROLE_USER...");
                    return roleRepository.save(Role.builder()
                            .name(Role.RoleName.ROLE_USER)
                            .build());
                });

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> {
                    log.info("Creating ROLE_ADMIN...");
                    return roleRepository.save(Role.builder()
                            .name(Role.RoleName.ROLE_ADMIN)
                            .build());
                });

        Role moderatorRole = roleRepository.findByName(Role.RoleName.ROLE_MODERATOR)
                .orElseGet(() -> {
                    log.info("Creating ROLE_MODERATOR...");
                    return roleRepository.save(Role.builder()
                            .name(Role.RoleName.ROLE_MODERATOR)
                            .build());
                });

        // Создание admin пользователя
        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating admin user...");
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .enabled(true)
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(admin);
            log.info("Admin user created successfully!");
        }

        // Создание тестового пользователя
        if (!userRepository.existsByUsername("user")) {
            log.info("Creating test user...");
            User testUser = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .firstName("Test")
                    .lastName("User")
                    .enabled(true)
                    .roles(Set.of(userRole))
                    .build();
            userRepository.save(testUser);
            log.info("Test user created successfully!");
        }
    }
}

