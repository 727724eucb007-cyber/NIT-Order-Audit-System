package com.nitaudit.config;

import com.nitaudit.model.FinishedGoodsLedger;
import com.nitaudit.model.Product;
import com.nitaudit.model.ProductionOrder;
import com.nitaudit.repository.FinishedGoodsLedgerRepository;
import com.nitaudit.repository.ProductionOrderRepository;
import com.nitaudit.repository.ProductRepository;
import com.nitaudit.service.FinishedGoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Runs after DataSeeder (Order 2 vs DataSeeder's default Order).
 * Idempotent: uses fgStockCredited flag so it never double-credits.
 * Seeds 50-unit opening stock for Murukku (SKU MUR-200) if no FG ledger
 * entries exist for that product yet.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class FinishedGoodsReconciler implements CommandLineRunner {

    private final ProductionOrderRepository productionOrderRepo;
    private final ProductRepository productRepo;
    private final FinishedGoodsLedgerRepository fgLedgerRepo;
    private final FinishedGoodsService fgService;

    @Override
    @Transactional
    public void run(String... args) {
        // Seed 50-unit opening stock for Murukku if it exists and has no FG ledger yet
        productRepo.findAll().stream()
            .filter(p -> "MUR-200".equals(p.getSku()) && !fgLedgerRepo.existsByProductId(p.getId()))
            .findFirst()
            .ifPresent(murukku -> {
                log.info("Seeding 50-unit opening FG stock for {}", murukku.getName());
                fgService.credit(murukku, new BigDecimal("50"), FinishedGoodsLedger.TxnType.OPENING, "Opening Stock");
            });

        // Credit all completed production orders that haven't been credited yet
        for (ProductionOrder order : productionOrderRepo.findAll()) {
            if (order.getStatus() == ProductionOrder.Status.COMPLETED && !order.isFgStockCredited()) {
                BigDecimal qty = order.getActualQty() != null ? order.getActualQty() : BigDecimal.ZERO;
                if (qty.compareTo(BigDecimal.ZERO) > 0) {
                    Product product = order.getProduct();
                    log.info("Reconciling FG stock: Production Order #{} -> {} units of {}",
                            order.getId(), qty, product.getName());
                    fgService.credit(product, qty, FinishedGoodsLedger.TxnType.PRODUCED,
                            "Production Order #" + order.getId());
                }
                order.setFgStockCredited(true);
                productionOrderRepo.save(order);
            }
        }
    }
}
