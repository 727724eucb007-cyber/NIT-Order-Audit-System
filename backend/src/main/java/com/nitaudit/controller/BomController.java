package com.nitaudit.controller;

import com.nitaudit.model.Bom;
import com.nitaudit.model.Product;
import com.nitaudit.model.RawMaterial;
import com.nitaudit.repository.BomRepository;
import com.nitaudit.repository.ProductRepository;
import com.nitaudit.repository.RawMaterialRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bom")
@RequiredArgsConstructor
public class BomController {
    private final BomRepository bomRepo;
    private final ProductRepository productRepo;
    private final RawMaterialRepository rmRepo;

    @GetMapping("/product/{productId}")
    public List<Bom> getByProduct(@PathVariable Long productId) {
        return bomRepo.findByProductId(productId);
    }

    @PostMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<List<Bom>> saveBom(@PathVariable Long productId,
                                              @RequestBody List<BomEntry> entries) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        bomRepo.deleteByProductId(productId);
        List<Bom> boms = entries.stream().map(e -> {
            RawMaterial rm = rmRepo.findById(e.getRawMaterialId())
                    .orElseThrow(() -> new RuntimeException("RM not found: " + e.getRawMaterialId()));
            Bom b = new Bom();
            b.setProduct(product);
            b.setRawMaterial(rm);
            b.setQtyRequired(e.getQtyRequired());
            return b;
        }).toList();
        return ResponseEntity.ok(bomRepo.saveAll(boms));
    }

    @Data
    static class BomEntry {
        private Long rawMaterialId;
        private BigDecimal qtyRequired;
    }
}
