package com.afrudeen.user.controller;

import com.afrudeen.user.dto.request.ChangePasswordRequest;
import com.afrudeen.user.dto.request.UpdateProfileRequest;
import com.afrudeen.user.dto.response.AuthResponse;
import com.afrudeen.user.dto.response.MessageResponse;
import com.afrudeen.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@Tag(name = "Profile")
public class ProfileController {

    private final AuthService service;

    public ProfileController(AuthService service) {
        this.service = service;
    }

    @PutMapping
    @Operation(summary = "Update my name / email (returns a fresh token)")
    public AuthResponse update(@RequestHeader("X-User-Id") Long userId,
                               @Valid @RequestBody UpdateProfileRequest request) {
        return service.updateProfile(userId, request);
    }

    @PutMapping("/password")
    @Operation(summary = "Change my password")
    public MessageResponse changePassword(@RequestHeader("X-User-Id") Long userId,
                                          @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(userId, request);
        return new MessageResponse("Password changed");
    }
}