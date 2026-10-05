package com.afrudeen.user.service;
import com.afrudeen.user.common.*;
import com.afrudeen.user.dto.request.ChangePasswordRequest;
import com.afrudeen.user.dto.request.UpdateProfileRequest;
import com.afrudeen.user.dto.response.AuthResponse;
import com.afrudeen.user.dto.response.LoginRequest;
import com.afrudeen.user.dto.request.RegisterRequest;
import com.afrudeen.user.dto.response.UserSummary;
import com.afrudeen.user.entity.*;
import com.afrudeen.user.enums.Role;
import com.afrudeen.user.repository.UserRepository;
import com.afrudeen.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (users.existsByEmail(registerRequest.email())) {
            throw new BusinessException("Email already registered");
        }
        User user = users.save(
                new User(registerRequest.name(),
                        registerRequest.email(),
                encoder.encode(registerRequest.password()),
                        Role.USER)
        ); // default role
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest registerRequest) {
        User user = users.findByEmail(registerRequest.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!encoder.matches(registerRequest.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password"); // same message!
        }

        return toResponse(user);
    }

    @Transactional
    public AuthResponse updateProfile(Long userId, UpdateProfileRequest r) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        String name = r.name().trim();
        String email = r.email().trim();

        if (!user.getEmail().equalsIgnoreCase(email)) {
            if (r.currentPassword() == null || !encoder.matches(r.currentPassword(), user.getPassword())) {
                throw new BusinessException("Current password is incorrect");
            }
            if (users.existsByEmail(email)) {
                throw new BusinessException("Email already registered");
            }
            user.setEmail(email);
        }

        user.setName(name);
        return toResponse(users.save(user));       // new token with the new name and email
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest r) {
        User user = users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        if (!encoder.matches(r.currentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        if (encoder.matches(r.newPassword(), user.getPassword())) {
            throw new BusinessException("New password must be different from the current one");
        }

        user.setPassword(encoder.encode(r.newPassword()));
        users.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> lookup(List<Long> ids) {
        if (ids.size() > 100) {
            throw new BusinessException("Too many ids");
        }
        return users.findAllById(ids).stream()
                .map(u -> new UserSummary(u.getId(), u.getName(), u.getEmail()))
                .toList();
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(
                jwt.generate(user),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

}
