package com.afrudeen.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String currentPassword) { }          // only needed when the email changes
