package com.afrudeen.order.client;

import com.afrudeen.order.common.BaseResponse;
import com.afrudeen.order.common.ResourceNotFoundException;
import com.afrudeen.order.dto.response.ProductResponse;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class ProductGateway {

    private final ProductClient client;

    public ProductGateway(ProductClient client) {
        this.client = client;
    }

    public ProductResponse getProduct(Long id) {
        try {
            BaseResponse<ProductResponse> response = client.findById(id);

            if (response == null || response.data() == null) {
                throw new ResourceNotFoundException(
                        "Product not found: " + id
                );
            }

            return response.data();

        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException(
                    "Product not found: " + id
            );
        }
    }
}