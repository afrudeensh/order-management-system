package com.afrudeen.order.client;

import com.afrudeen.order.common.BaseResponse;
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

    private static final Logger log =
            LoggerFactory.getLogger(ProductGateway.class);

    private final ProductClient client;

    public ProductGateway(ProductClient client) {
        this.client = client;
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "fallback"
    )
    public ProductResponse getProduct(Long id) {

        try {
            BaseResponse<ProductResponse> response =
                    client.findById(id);

            return response.data();

        } catch (FeignException.NotFound e) {

            throw new ResourceNotFoundException(
                    "Product not found: " + id
            );
        }
    }

    private ProductResponse fallback(Long id, Throwable ex) {

        if (ex instanceof ResourceNotFoundException notFound) {
            throw notFound;
        }

        log.warn(
                "Product service unavailable for product {}: {}",
                id,
                ex.toString()
        );

        throw new ServiceUnavailableException(
                "Product service is unavailable right now. " +
                        "Please try again shortly."
        );
    }
}