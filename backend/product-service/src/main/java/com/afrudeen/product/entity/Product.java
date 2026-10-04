package com.afrudeen.product.entity;

import com.afrudeen.product.common.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String image;        // small data URL, optional

    @Column(length = 7)
    private String color;        // "#RRGGBB", optional

    protected Product() { }      // required by JPA

    public Product(String name, BigDecimal price, Integer stock) {
        this(name, price, stock, null, null);
    }

    public Product(String name, BigDecimal price, Integer stock,
                   String image, String color) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.color = color;
    }

    @Override
    public String getDisplayName() { return name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}