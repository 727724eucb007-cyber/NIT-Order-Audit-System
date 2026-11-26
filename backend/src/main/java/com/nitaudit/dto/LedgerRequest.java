package com.nitaudit.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LedgerRequest {
    private Long rawMaterialId;
    private String txnType; // PURCHASE or ADJUSTMENT
    private BigDecimal qty;
    private String reference;
}
