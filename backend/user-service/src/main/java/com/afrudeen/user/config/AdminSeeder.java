package com.afrudeen.user.config;

import com.afrudeen.user.entity.*;
import com.afrudeen.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.afrudeen.user.enums.Role;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                @Value("${app.admin.email}") String email,
                                @Value("${app.admin.password}") String password) {

        return args -> {

            if (!userRepository.existsByEmail(email)) {
                userRepository.save(new User("Admin",
                        email,
                        passwordEncoder.encode(password),
                        Role.ADMIN));
            }
        };

    }
}
