package com.nitaudit.controller;

import com.nitaudit.dto.ProductionOrderRequest;
import com.nitaudit.model.ProductionOrder;
import com.nitaudit.model.User;
import com.nitaudit.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionController {
    private final ProductionService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public List<ProductionOrder> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ProductionOrder> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ProductionOrder> create(@RequestBody ProductionOrderRequest req,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.createOrder(req, user.getId()));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ProductionOrder> start(@PathVariable Long id) {
        return ResponseEntity.ok(service.startOrder(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<ProductionOrder> complete(@PathVariable Long id,
                                                     @RequestBody ProductionOrderRequest req) {
        return ResponseEntity.ok(service.completeOrder(id, req));
    }
}
