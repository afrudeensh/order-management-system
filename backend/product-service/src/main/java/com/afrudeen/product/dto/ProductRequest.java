package com.afrudeen.product.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(

        @Schema(example = "Fuel Injector")
        @NotBlank
        String name,

        @Schema(example = "1299.00")
        @NotNull
        @DecimalMin("0.01")
        BigDecimal price,

        @Schema(example = "20")
        @NotNull
        @Min(0)
        Integer stock) {
}