package com.nitaudit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_ledger")
@Data
@NoArgsConstructor
public class InventoryLedger {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;

    @Enumerated(EnumType.STRING)
    private TxnType txnType;

    @Column(precision = 12, scale = 4)
    private BigDecimal qty;

    @Column(precision = 12, scale = 4)
    private BigDecimal balanceAfter;

    private String reference;
    private LocalDateTime txnDate = LocalDateTime.now();

    public enum TxnType { PURCHASE, CONSUMED, ADJUSTMENT }
}
