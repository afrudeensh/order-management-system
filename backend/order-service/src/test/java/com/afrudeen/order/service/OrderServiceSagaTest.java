package com.afrudeen.order.service;

import com.afrudeen.order.client.ProductGateway;
import com.afrudeen.order.common.BusinessException;
import com.afrudeen.order.dto.request.CreateOrderRequest;
import com.afrudeen.order.dto.request.OrderItemRequest;
import com.afrudeen.order.dto.response.ProductResponse;
import com.afrudeen.order.entity.Order;
import com.afrudeen.order.event.OrderEventPublisher;
import com.afrudeen.order.repository.OrderRepository;
import com.afrudeen.order.saga.StockCompensator;
import com.afrudeen.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceSagaTest {

    @Mock OrderRepository repository;
    @Mock ProductGateway products;
    @Mock OrderEventPublisher events;
    @Mock StockCompensator compensator;

    @InjectMocks OrderServiceImpl service;

    private ProductResponse product(Long id, String name, String price, int stock) {
        ProductResponse p = mock(ProductResponse.class);
        lenient().when(p.id()).thenReturn(id);
        lenient().when(p.name()).thenReturn(name);
        lenient().when(p.price()).thenReturn(new BigDecimal(price));
        lenient().when(p.stock()).thenReturn(stock);
        return p;
    }

    @Test
    void create_whenSecondReservationFails_restoresTheFirst() {
        when(products.getProduct(1L)).thenReturn(product(1L, "Oil Filter", "100.00", 10));
        when(products.getProduct(2L)).thenReturn(product(2L, "Air Filter", "50.00", 10));
        doThrow(new BusinessException("Insufficient stock for product 2"))
                .when(products).decreaseStock(2L, 3);

        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(2L, 3)));

        assertThrows(BusinessException.class, () -> service.create(7L, request));

        verify(products).decreaseStock(1L, 2);                          // first line was reserved
        verify(compensator).restore(eq(1L), eq(2), anyString());        // ...and given back
        verify(compensator, never()).restore(eq(2L), anyInt(), anyString());
        verify(repository, never()).save(any(Order.class));              // no order saved
        verifyNoInteractions(events);                                    // no event published
    }

    @Test
    void create_whenPublishingFails_restoresAllReservedLines() {
        when(products.getProduct(1L)).thenReturn(product(1L, "Oil Filter", "100.00", 10));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new IllegalStateException("kafka down")).when(events).publish(any());

        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(1L, 4)));

        assertThrows(IllegalStateException.class, () -> service.create(7L, request));

        verify(compensator).restore(eq(1L), eq(4), anyString());
    }
}