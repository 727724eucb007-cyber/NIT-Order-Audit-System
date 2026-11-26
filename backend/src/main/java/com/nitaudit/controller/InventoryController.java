package com.nitaudit.controller;

import com.nitaudit.dto.LedgerRequest;
import com.nitaudit.model.InventoryLedger;
import com.nitaudit.model.RawMaterial;
import com.nitaudit.repository.InventoryLedgerRepository;
import com.nitaudit.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryLedgerRepository ledgerRepo;
    private final RawMaterialRepository rmRepo;

    @GetMapping("/ledger")
    public List<InventoryLedger> getLedger() {
        return ledgerRepo.findAllByOrderByTxnDateDesc();
    }

    @GetMapping("/ledger/{rawMaterialId}")
    public List<InventoryLedger> getLedgerByRM(@PathVariable Long rawMaterialId) {
        return ledgerRepo.findByRawMaterialIdOrderByTxnDateDesc(rawMaterialId);
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    @Transactional
    public ResponseEntity<InventoryLedger> purchase(@RequestBody LedgerRequest req) {
        RawMaterial rm = rmRepo.findById(req.getRawMaterialId())
                .orElseThrow(() -> new RuntimeException("Raw material not found"));
        BigDecimal newStock = rm.getCurrentStock().add(req.getQty());
        rm.setCurrentStock(newStock);
        rmRepo.save(rm);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setRawMaterial(rm);
        ledger.setTxnType(InventoryLedger.TxnType.PURCHASE);
        ledger.setQty(req.getQty());
        ledger.setBalanceAfter(newStock);
        ledger.setReference(req.getReference() != null ? req.getReference() : "Purchase");
        return ResponseEntity.ok(ledgerRepo.save(ledger));
    }

    @PostMapping("/adjustment")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<InventoryLedger> adjustment(@RequestBody LedgerRequest req) {
        RawMaterial rm = rmRepo.findById(req.getRawMaterialId())
                .orElseThrow(() -> new RuntimeException("Raw material not found"));
        BigDecimal newStock = rm.getCurrentStock().add(req.getQty());
        rm.setCurrentStock(newStock);
        rmRepo.save(rm);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setRawMaterial(rm);
        ledger.setTxnType(InventoryLedger.TxnType.ADJUSTMENT);
        ledger.setQty(req.getQty());
        ledger.setBalanceAfter(newStock);
        ledger.setReference(req.getReference() != null ? req.getReference() : "Adjustment");
        return ResponseEntity.ok(ledgerRepo.save(ledger));
    }
}
