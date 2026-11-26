package com.nitaudit.service;

import com.nitaudit.model.FinishedGoodsLedger;
import com.nitaudit.model.Product;
import com.nitaudit.repository.FinishedGoodsLedgerRepository;
import com.nitaudit.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FinishedGoodsService {

    private final ProductRepository productRepo;
    private final FinishedGoodsLedgerRepository fgLedgerRepo;

    /** Returns the product's finishedStock, treating DB NULL as ZERO. */
    private BigDecimal stock(Product product) {
        return product.getFinishedStock() != null ? product.getFinishedStock() : BigDecimal.ZERO;
    }

    @Transactional
    public void credit(Product product, BigDecimal qty, FinishedGoodsLedger.TxnType type, String reference) {
        product.setFinishedStock(stock(product).add(qty));
        productRepo.save(product);
        writeLedger(product, qty, type, reference);
    }

    @Transactional
    public void debit(Product product, BigDecimal qty, String reference) {
        BigDecimal current = stock(product);
        if (current.compareTo(qty) < 0)
            throw new RuntimeException("Insufficient finished-goods stock for " + product.getName()
                    + ". Available: " + current + ", Required: " + qty);
        product.setFinishedStock(current.subtract(qty));
        productRepo.save(product);
        writeLedger(product, qty.negate(), FinishedGoodsLedger.TxnType.DISPATCHED, reference);
    }

    private void writeLedger(Product product, BigDecimal qty, FinishedGoodsLedger.TxnType type, String reference) {
        FinishedGoodsLedger entry = new FinishedGoodsLedger();
        entry.setProduct(product);
        entry.setTxnType(type);
        entry.setQty(qty);
        entry.setBalanceAfter(product.getFinishedStock());
        entry.setReference(reference);
        fgLedgerRepo.save(entry);
    }
}
