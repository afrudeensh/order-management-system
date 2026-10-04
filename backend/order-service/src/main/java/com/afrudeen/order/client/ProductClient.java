package com.afrudeen.order.client;

import com.afrudeen.order.common.BaseResponse;
import com.afrudeen.order.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    BaseResponse<ProductResponse> findById(
            @PathVariable("id") Long id
    );

    @PutMapping("/products/{id}/stock/decrease")
    void decreaseStock(
            @PathVariable("id") Long id,
            @RequestParam("quantity") int quantity
    );
}