package com.nitaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "production_consumption")
@Getter
@Setter
@NoArgsConstructor
public class ProductionConsumption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    @JsonIgnoreProperties({"consumptions", "hibernateLazyInitializer", "handler"})
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;

    @Column(precision = 12, scale = 4)
    private BigDecimal plannedQty;

    @Column(precision = 12, scale = 4)
    private BigDecimal actualQty;
}
