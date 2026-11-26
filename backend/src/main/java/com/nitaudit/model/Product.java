package com.nitaudit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String sku;
    private String unit;
    @Column(precision = 10, scale = 2)
    private BigDecimal sellingPrice;
    @Column(precision = 5, scale = 2)
    private BigDecimal gstRate;
    private boolean active = true;
    @Column(precision = 12, scale = 3, columnDefinition = "DECIMAL(12,3) DEFAULT 0")
    private BigDecimal finishedStock = BigDecimal.ZERO;
}
