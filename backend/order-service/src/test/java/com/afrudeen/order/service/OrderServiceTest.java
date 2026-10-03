package com.afrudeen.order.service;

import com.afrudeen.order.client.ProductGateway;
import com.afrudeen.order.common.BusinessException;
import com.afrudeen.order.dto.request.CreateOrderRequest;
import com.afrudeen.order.dto.request.OrderItemRequest;
import com.afrudeen.order.dto.response.OrderResponse;
import com.afrudeen.order.dto.response.ProductResponse;
import com.afrudeen.order.entity.Order;
import com.afrudeen.order.event.OrderEventPublisher;
import com.afrudeen.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    OrderRepository repository;

    @Mock
    ProductGateway products;

    @Mock
    OrderEventPublisher events;

    @InjectMocks
    OrderServiceImpl service;

    @Test
    void create_calculatesTotalFromProductServicePrice() {
        when(products.getProduct(1L))
                .thenReturn(new ProductResponse(1L, "Oil Filter", new BigDecimal("349.50"), 10));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        OrderResponse r = service.create(7L,
                new CreateOrderRequest(List.of(new OrderItemRequest(1L, 2))));
        assertEquals(new BigDecimal("699.00"), r.totalAmount());
        verify(events).publish(any());
    }
    @Test
    void create_whenStockTooLow_throwsAndSavesNothing() {
        when(products.getProduct(1L))
                .thenReturn(new ProductResponse(1L, "Oil Filter", new BigDecimal("349.50"), 1));
        assertThrows(BusinessException.class, () -> service.create(7L,
                new CreateOrderRequest(List.of(new OrderItemRequest(1L, 5)))));
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }
}
