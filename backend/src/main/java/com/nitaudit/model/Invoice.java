package com.nitaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id", unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SalesOrder salesOrder;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    private LocalDate invoiceDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal gstAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;
}
