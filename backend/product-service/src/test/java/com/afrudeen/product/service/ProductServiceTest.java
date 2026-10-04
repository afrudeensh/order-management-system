package com.afrudeen.product.service;

import com.afrudeen.product.common.BusinessException;
import com.afrudeen.product.common.ResourceNotFoundException;
import com.afrudeen.product.dto.ProductRequest;
import com.afrudeen.product.entity.Product;
import com.afrudeen.product.repository.ProductRepository;
import com.afrudeen.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository repository;

    @InjectMocks
    ProductServiceImpl service;

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(repository.findByIdAndIsActive(99L, true))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));
    }

    @Test
    void create_savesProduct_withTrimmedName() {
        when(repository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Product saved = service.create(new ProductRequest(
                "  Mouse  ", new BigDecimal("599.00"), 5, null, "#4f46e5"));

        assertEquals("Mouse", saved.getName());
        assertEquals("#4f46e5", saved.getColor());
        verify(repository).save(any(Product.class));
    }

    @Test
    void create_duplicateName_throwsBusinessException() {
        when(repository.existsByNameIgnoreCase("Mouse")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(
                new ProductRequest("Mouse", new BigDecimal("599.00"), 5, null, null)));

        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void update_changesPriceAndColor_butNeverStock() {
        Product existing = new Product("Mouse", new BigDecimal("500.00"), 10);
        when(repository.findByIdAndIsActive(1L, true)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = service.update(1L, new ProductRequest(
                "Mouse", new BigDecimal("650.00"), 999, null, "#112233"));

        assertEquals(new BigDecimal("650.00"), updated.getPrice());
        assertEquals("#112233", updated.getColor());
        assertEquals(10, updated.getStock());      // the stock in the request is ignored
    }

    @Test
    void decreaseStock_whenNotEnough_throwsBusinessException() {
        when(repository.findByIdAndIsActive(1L, true))
                .thenReturn(Optional.of(new Product("Mouse", new BigDecimal("500.00"), 2)));
        when(repository.decreaseStock(1L, 5)).thenReturn(0);

        assertThrows(BusinessException.class, () -> service.decreaseStock(1L, 5));
    }
}