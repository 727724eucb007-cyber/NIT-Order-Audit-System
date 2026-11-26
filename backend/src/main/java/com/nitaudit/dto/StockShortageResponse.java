package com.nitaudit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class StockShortageResponse {
    private final String message;
    private final List<ShortageItem> shortages;

    @Getter
    @AllArgsConstructor
    public static class ShortageItem {
        private final String productName;
        private final BigDecimal required;
        private final BigDecimal available;
        private final BigDecimal shortage;
    }
}
