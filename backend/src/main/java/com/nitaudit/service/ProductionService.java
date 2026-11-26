package com.nitaudit.service;

import com.nitaudit.dto.ProductionOrderRequest;
import com.nitaudit.model.*;
import com.nitaudit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionService {
    private final ProductionOrderRepository orderRepo;
    private final ProductionConsumptionRepository consumptionRepo;
    private final BomRepository bomRepo;
    private final RawMaterialRepository rmRepo;
    private final ProductRepository productRepo;
    private final InventoryLedgerRepository ledgerRepo;
    private final UserRepository userRepo;
    private final FinishedGoodsService fgService;

    @Transactional
    public ProductionOrder createOrder(ProductionOrderRequest req, Long userId) {
        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductionOrder order = new ProductionOrder();
        order.setProduct(product);
        order.setPlannedQty(req.getPlannedQty());
        order.setStatus(ProductionOrder.Status.PLANNED);
        order.setCreatedBy(user);
        order.setNotes(req.getNotes());
        order = orderRepo.save(order);

        // Create planned consumptions from BOM
        List<Bom> boms = bomRepo.findByProductId(product.getId());
        for (Bom bom : boms) {
            ProductionConsumption pc = new ProductionConsumption();
            pc.setProductionOrder(order);
            pc.setRawMaterial(bom.getRawMaterial());
            pc.setPlannedQty(bom.getQtyRequired().multiply(req.getPlannedQty()));
            pc.setActualQty(BigDecimal.ZERO);
            consumptionRepo.save(pc);
        }
        return order;
    }

    @Transactional
    public ProductionOrder startOrder(Long orderId) {
        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != ProductionOrder.Status.PLANNED)
            throw new RuntimeException("Order is not in PLANNED state");
        order.setStatus(ProductionOrder.Status.IN_PROGRESS);
        return orderRepo.save(order);
    }

    @Transactional
    public ProductionOrder completeOrder(Long orderId, ProductionOrderRequest req) {
        ProductionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == ProductionOrder.Status.COMPLETED)
            throw new RuntimeException("Order already completed");

        order.setActualQty(req.getActualQty());
        order.setStatus(ProductionOrder.Status.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        // Credit finished-goods stock
        if (req.getActualQty() != null && req.getActualQty().compareTo(BigDecimal.ZERO) > 0) {
            fgService.credit(order.getProduct(), req.getActualQty(),
                    FinishedGoodsLedger.TxnType.PRODUCED, "Production Order #" + orderId);
        }
        order.setFgStockCredited(true);

        // Update actual consumptions and deduct stock
        List<ProductionConsumption> consumptions = consumptionRepo.findByProductionOrderId(orderId);
        for (ProductionOrderRequest.ConsumptionEntry entry : req.getConsumptions()) {
            consumptions.stream()
                .filter(c -> c.getRawMaterial().getId().equals(entry.getRawMaterialId()))
                .findFirst()
                .ifPresent(c -> {
                    c.setActualQty(entry.getActualQty());
                    consumptionRepo.save(c);

                    // Deduct from raw material stock
                    RawMaterial rm = c.getRawMaterial();
                    BigDecimal newStock = rm.getCurrentStock().subtract(entry.getActualQty());
                    rm.setCurrentStock(newStock);
                    rmRepo.save(rm);

                    // Ledger entry
                    InventoryLedger ledger = new InventoryLedger();
                    ledger.setRawMaterial(rm);
                    ledger.setTxnType(InventoryLedger.TxnType.CONSUMED);
                    ledger.setQty(entry.getActualQty().negate());
                    ledger.setBalanceAfter(newStock);
                    ledger.setReference("Production Order #" + orderId);
                    ledgerRepo.save(ledger);
                });
        }
        return orderRepo.save(order);
    }

    public List<ProductionOrder> getAll() { return orderRepo.findAll(); }

    public ProductionOrder getById(Long id) {
        return orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
