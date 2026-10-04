package com.afrudeen.order.service.impl;

import com.afrudeen.order.client.ProductGateway;
import com.afrudeen.order.common.BusinessException;
import com.afrudeen.order.common.ForbiddenException;
import com.afrudeen.order.common.ResourceNotFoundException;
import com.afrudeen.order.dto.request.CreateOrderRequest;
import com.afrudeen.order.dto.request.OrderItemRequest;
import com.afrudeen.order.dto.response.OrderResponse;
import com.afrudeen.order.dto.response.ProductResponse;
import com.afrudeen.order.entity.Order;
import com.afrudeen.order.entity.OrderItem;
import com.afrudeen.order.enums.OrderStatus;
import com.afrudeen.order.event.OrderCreatedEvent;
import com.afrudeen.order.event.OrderEventPublisher;
import com.afrudeen.order.repository.OrderRepository;
import com.afrudeen.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final ProductGateway products;
    private final OrderEventPublisher events;


    public OrderServiceImpl(OrderRepository repository, ProductGateway products, OrderEventPublisher events) {
        this.repository = repository;
        this.products = products;
        this.events = events;
    }

    @Override
    public OrderResponse create(Long userId, CreateOrderRequest request) {
        Order order = new Order(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest line : request.items()) {
            ProductResponse p = products.getProduct(line.productId()); // sync call
            System.out.println("PRODUCT RESPONSE = " + p);

            if (p.stock() < line.quantity()) {
                throw new BusinessException("Insufficient stock for " + p.name());
            }

            order.addItem(new OrderItem(p.id(), p.name(), line.quantity(), p.price()));
            total = total.add(p.price().multiply(BigDecimal.valueOf(line.quantity())));
        }

        order.setTotalAmount(total); // total computed on the SERVER
        Order saved = repository.save(order);

        for (OrderItemRequest line : request.items()) {
            products.decreaseStock(line.productId(), line.quantity());
        }

        events.publish(new OrderCreatedEvent(saved.getId(), saved.getUserId(),
                saved.getTotalAmount(), saved.getCreatedAt()));
        return OrderResponse.from(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id, Long userId, boolean admin) {

        Order o = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (!admin && !o.getUserId().equals(userId)) {
            throw new ForbiddenException("This order belongs to another user");
        }

        return OrderResponse.from(o);
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findMine(Long userId) {

        return repository.findByUserWithItems(userId)
                .stream().map(OrderResponse::from).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return repository.findAll()
                .stream().map(OrderResponse::from).toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            throw new BusinessException("Use the cancel action to cancel an order");
        }
        Order o = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        o.changeStatus(status);
        return OrderResponse.from(o);
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long id, Long userId, boolean admin) {
        Order o = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (!admin && !o.getUserId().equals(userId)) {
            throw new ForbiddenException("This order belongs to another user");
        }
        if (!admin && o.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Only orders that are not confirmed yet can be cancelled");
        }

        o.changeStatus(OrderStatus.CANCELLED);       // also checks the move is allowed

        for (OrderItem item : o.getItems()) {
            products.increaseStock(item.getProductId(), item.getQuantity());
        }
        return OrderResponse.from(o);
    }
}
