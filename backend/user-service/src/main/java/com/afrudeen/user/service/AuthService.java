package com.afrudeen.user.service;
import com.afrudeen.user.common.*;
import com.afrudeen.user.dto.*;
import com.afrudeen.user.entity.*;
import com.afrudeen.user.enums.Role;
import com.afrudeen.user.repository.UserRepository;
import com.afrudeen.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private AuthResponse toResponse(User user) {
        return new AuthResponse(
                jwt.generate(user),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

}
