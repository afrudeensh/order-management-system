package com.afrudeen.order.dto.request;


import com.afrudeen.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @NotNull
        OrderStatus status) { }