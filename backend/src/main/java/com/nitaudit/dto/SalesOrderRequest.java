package com.nitaudit.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesOrderRequest {
    private Long customerId;
    private String notes;
    private List<ItemEntry> items;

    @Data
    public static class ItemEntry {
        private Long productId;
        private BigDecimal qty;
        private BigDecimal unitPrice;
        private BigDecimal gstRate;
    }
}
