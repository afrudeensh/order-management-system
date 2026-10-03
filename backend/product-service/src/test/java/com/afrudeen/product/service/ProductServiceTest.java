package com.afrudeen.product.service;

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

        when(repository.findById(99L))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));
    }

    @Test
    void create_savesProduct() {

        when(repository.save(any(Product.class)))
                .thenAnswer(inv
                        -> inv.getArgument(0));

        Product saved = service.create(new ProductRequest
                ("Mouse", new BigDecimal("599.00"), 5));

        assertEquals("Mouse", saved.getName());
        verify(repository).save(any(Product.class));
    }
}