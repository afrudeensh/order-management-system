package com.afrudeen.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank
        String name,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal price,

        @NotNull
        @Min(0)
        Integer stock,

        @Size(max = 300000, message = "Image is too large")
        String image,

        @Pattern(
                regexp = "^#[0-9a-fA-F]{6}$",
                message = "Color must look like #RRGGBB"
        )
        String color

) {
}