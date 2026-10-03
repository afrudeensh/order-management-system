package com.afrudeen.product.controller;

import com.afrudeen.product.common.ResourceNotFoundException;
import com.afrudeen.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProductServiceImpl service; // replaces the real bean with a mock

    @Test
    void getById_missing_returns404Json() throws Exception {

        when(service.findById(9L)).thenThrow(new ResourceNotFoundException("Product not found: 9"));

        mvc.perform(get("/products/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: 9"));
    }

    @Test
    void create_invalidBody_returns400() throws Exception {

        String bad = "{\"name\":\"\",\"price\":-1,\"stock\":1}";

        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }
}