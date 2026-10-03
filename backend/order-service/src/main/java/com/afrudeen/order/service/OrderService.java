package com.afrudeen.order.service;

import com.afrudeen.order.dto.request.CreateOrderRequest;
import com.afrudeen.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(Long userId, CreateOrderRequest request);

    OrderResponse findById(Long id, Long userId, boolean admin);

    List<OrderResponse> findMine(Long userId);

    List<OrderResponse> findAll();
}
