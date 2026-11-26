package com.nitaudit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "raw_materials")
@Data
@NoArgsConstructor
public class RawMaterial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String unit;
    @Column(precision = 12, scale = 3)
    private BigDecimal currentStock = BigDecimal.ZERO;
    @Column(precision = 12, scale = 3)
    private BigDecimal reorderLevel = BigDecimal.ZERO;
    @Column(precision = 10, scale = 2)
    private BigDecimal costPerUnit = BigDecimal.ZERO;
}
