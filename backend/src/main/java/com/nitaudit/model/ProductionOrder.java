package com.nitaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
public class ProductionOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(precision = 12, scale = 3)
    private BigDecimal plannedQty;

    @Column(precision = 12, scale = 3)
    private BigDecimal actualQty;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PLANNED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    private String notes;

    private boolean fgStockCredited = false;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"productionOrder"})
    private List<ProductionConsumption> consumptions = new ArrayList<>();

    public enum Status { PLANNED, IN_PROGRESS, COMPLETED }
}
