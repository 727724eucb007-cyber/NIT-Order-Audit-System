package com.nitaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finished_goods_ledger")
@Getter
@Setter
@NoArgsConstructor
public class FinishedGoodsLedger {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Enumerated(EnumType.STRING)
    private TxnType txnType;

    @Column(precision = 12, scale = 3)
    private BigDecimal qty;

    @Column(precision = 12, scale = 3)
    private BigDecimal balanceAfter;

    private String reference;
    private LocalDateTime txnDate = LocalDateTime.now();

    public enum TxnType { OPENING, PRODUCED, DISPATCHED, ADJUSTMENT }
}
