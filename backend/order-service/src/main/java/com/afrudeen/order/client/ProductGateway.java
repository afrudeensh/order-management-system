package com.afrudeen.order.client;

import com.afrudeen.order.common.BaseResponse;
import com.afrudeen.order.common.BusinessException;
import com.afrudeen.order.common.ResourceNotFoundException;
import com.afrudeen.order.common.ServiceUnavailableException;
import com.afrudeen.order.dto.response.ProductResponse;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductGateway {

    private static final Logger log = LoggerFactory.getLogger(ProductGateway.class);

    private final ProductClient client;

    public ProductGateway(ProductClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallback")
    public ProductResponse getProduct(Long id) {
        try {
            BaseResponse<ProductResponse> response = client.findById(id);
            return response.data();
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
    }

    private ProductResponse fallback(Long id, Throwable ex) {
        if (ex instanceof ResourceNotFoundException notFound) {
            throw notFound;
        }

        log.warn("Product service unavailable for product {}: {}", id, ex.toString());

        throw new ServiceUnavailableException(
                "Product service is unavailable right now. Please try again shortly.");
    }

    public void decreaseStock(Long id, int quantity) {
        try {
            client.decreaseStock(id, quantity);

        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product not found: " + id);

        } catch (FeignException e) {
            log.warn("decreaseStock failed for product {} (status {}): {}",
                    id, e.status(), e.getMessage());

            // 4xx = the product-service rejected it (e.g. insufficient stock)
            if (e.status() >= 400 && e.status() < 500) {
                throw new BusinessException("Insufficient stock for product " + id);
            }
            // timeout / connection refused (status -1) / 5xx = service problem
            throw new ServiceUnavailableException(
                    "Product service is unavailable right now. Please try again shortly.");
        }
    }

    public void increaseStock(Long id, int quantity) {
        try {
            client.increaseStock(id, quantity);

        } catch (FeignException e) {
            log.warn("increaseStock failed for product {} (status {}): {}",
                    id, e.status(), e.getMessage());
            throw new BusinessException("Could not return stock for product " + id);
        }
    }
}