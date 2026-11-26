package com.nitaudit.service;

import com.nitaudit.dto.StockShortageResponse;
import lombok.Getter;
import java.util.List;

@Getter
public class StockShortageException extends RuntimeException {
    private final List<StockShortageResponse.ShortageItem> shortages;

    public StockShortageException(List<StockShortageResponse.ShortageItem> shortages) {
        super("Insufficient finished-goods stock");
        this.shortages = shortages;
    }
}
