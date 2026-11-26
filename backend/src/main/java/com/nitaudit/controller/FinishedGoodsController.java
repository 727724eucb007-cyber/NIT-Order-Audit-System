package com.nitaudit.controller;

import com.nitaudit.model.FinishedGoodsLedger;
import com.nitaudit.model.Product;
import com.nitaudit.repository.FinishedGoodsLedgerRepository;
import com.nitaudit.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/finished-goods")
@RequiredArgsConstructor
public class FinishedGoodsController {

    private final FinishedGoodsLedgerRepository fgLedgerRepo;
    private final ProductRepository productRepo;

    @GetMapping("/ledger")
    public List<FinishedGoodsLedger> getLedger() {
        return fgLedgerRepo.findAllByOrderByTxnDateDesc();
    }

    @GetMapping("/ledger/{productId}")
    public List<FinishedGoodsLedger> getLedgerByProduct(@PathVariable Long productId) {
        return fgLedgerRepo.findByProductIdOrderByTxnDateDesc(productId);
    }

    @GetMapping("/stock")
    public List<Product> getStock() {
        return productRepo.findAll();
    }
}
