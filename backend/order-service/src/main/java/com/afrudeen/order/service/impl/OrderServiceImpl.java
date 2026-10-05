package com.afrudeen.order.service.impl;

import com.afrudeen.order.client.ProductGateway;
import com.afrudeen.order.common.BusinessException;
import com.afrudeen.order.common.ForbiddenException;
import com.afrudeen.order.common.ResourceNotFoundException;
import com.afrudeen.order.dto.request.CreateOrderRequest;
import com.afrudeen.order.dto.request.OrderItemRequest;
import com.afrudeen.order.dto.response.OrderResponse;
import com.afrudeen.order.dto.response.PageResponse;
import com.afrudeen.order.dto.response.ProductResponse;
import com.afrudeen.order.entity.Order;
import com.afrudeen.order.entity.OrderItem;
import com.afrudeen.order.enums.OrderStatus;
import com.afrudeen.order.event.OrderCreatedEvent;
import com.afrudeen.order.event.OrderEventPublisher;
import com.afrudeen.order.repository.OrderRepository;
import com.afrudeen.order.saga.StockCompensator;
import com.afrudeen.order.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int LOW_STOCK_LIMIT = 15;

    private final OrderRepository repository;
    private final ProductGateway products;
    private final OrderEventPublisher events;
    private final StockCompensator compensator;

    public OrderServiceImpl(OrderRepository repository,
                            ProductGateway products,
                            OrderEventPublisher events,
                            StockCompensator compensator) {
        this.repository = repository;
        this.products = products;
        this.events = events;
        this.compensator = compensator;
    }

    @Override
    @Transactional
    public OrderResponse create(Long userId, CreateOrderRequest request) {
        Order order = new Order(userId);
        BigDecimal total = BigDecimal.ZERO;
        List<String> lowStock = new ArrayList<>();

        // Step 1: price the order and check stock (read-only, nothing to undo)
        for (OrderItemRequest line : request.items()) {
            ProductResponse p = products.getProduct(line.productId());

            if (p.stock() < line.quantity()) {
                throw new BusinessException("Insufficient stock for " + p.name());
            }

            int remaining = p.stock() - line.quantity();
            if (remaining < LOW_STOCK_LIMIT) {
                lowStock.add("%s (%d left)".formatted(p.name(), remaining));
            }

            order.addItem(new OrderItem(p.id(), p.name(), line.quantity(), p.price()));
            total = total.add(p.price().multiply(BigDecimal.valueOf(line.quantity())));
        }
        order.setTotalAmount(total);                 // total computed on the SERVER

        // Step 2: reserve stock, remembering every line that succeeded
        List<OrderItemRequest> reserved = new ArrayList<>();
        try {
            for (OrderItemRequest line : request.items()) {
                products.decreaseStock(line.productId(), line.quantity());
                reserved.add(line);
            }

            // Step 3: save the order and announce it
            Order saved = repository.save(order);
            events.publish(new OrderCreatedEvent(saved.getId(), saved.getUserId(),
                    saved.getTotalAmount(), saved.getCreatedAt(), lowStock));

            return OrderResponse.from(saved);

        } catch (RuntimeException e) {
            // Compensate: undo every reservation that already happened
            for (OrderItemRequest line : reserved) {
                compensator.restore(line.productId(), line.quantity(),
                        "order creation failed for user " + userId);
            }
            throw e;        // the order insert rolls back with the transaction
        }
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

        // Copy what we need while the items are loaded
        List<Restore> restores = o.getItems().stream()
                .map(i -> new Restore(i.getProductId(), i.getQuantity()))
                .toList();
        String reason = "order #" + id + " cancelled";

        // Give the stock back only after the cancellation is safely committed
        runAfterCommit(() -> restores.forEach(r ->
                compensator.restore(r.productId(), r.quantity(), reason)));

        return OrderResponse.from(o);
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
        return repository.findAll()          // keep your own version if you changed this one
                .stream().map(OrderResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> findMinePage(Long userId, Pageable pageable) {
        return PageResponse.from(repository.findByUserId(userId, pageable).map(OrderResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> findAllPage(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(OrderResponse::from));
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private record Restore(Long productId, int quantity) { }
}