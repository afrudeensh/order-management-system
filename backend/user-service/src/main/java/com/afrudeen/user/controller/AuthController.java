package com.afrudeen.user.controller;

import com.afrudeen.user.common.BaseResponse;
import com.afrudeen.user.dto.AuthResponse;
import com.afrudeen.user.dto.LoginRequest;
import com.afrudeen.user.dto.RegisterRequest;
import com.afrudeen.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Auth", description = "Register and login")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new customer (role USER)")
    public BaseResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = service.register(request);

        return BaseResponse.created(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT")
    public BaseResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = service.login(request);

        return BaseResponse.ok("Login successful", response);
    }
}