package com.nitaudit.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductionOrderRequest {
    private Long productId;
    private BigDecimal plannedQty;
    private String notes;
    // For completing: actual qty and actual consumptions
    private BigDecimal actualQty;
    private List<ConsumptionEntry> consumptions;

    @Data
    public static class ConsumptionEntry {
        private Long rawMaterialId;
        private BigDecimal actualQty;
    }
}
