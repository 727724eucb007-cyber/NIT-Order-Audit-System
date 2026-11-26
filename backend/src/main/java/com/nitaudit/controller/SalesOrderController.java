package com.nitaudit.controller;

import com.nitaudit.dto.SalesOrderRequest;
import com.nitaudit.dto.StockShortageResponse;
import com.nitaudit.model.*;
import com.nitaudit.service.SalesOrderService;
import com.nitaudit.service.StockShortageException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {
    private final SalesOrderService service;

    @GetMapping
    public List<SalesOrder> getAll(@AuthenticationPrincipal User user) {
        if (user.getRole() == User.Role.SALESMAN)
            return service.getBySalesman(user.getId());
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrder> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALESMAN')")
    public ResponseEntity<SalesOrder> create(@RequestBody SalesOrderRequest req,
                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.createOrder(req, user.getId()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.approveOrder(id));
        } catch (StockShortageException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new StockShortageResponse("Insufficient stock to approve order", ex.getShortages()));
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesOrder> reject(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.rejectOrder(id, body.getOrDefault("reason", "")));
    }

    @PutMapping("/{id}/dispatch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesOrder> dispatch(@PathVariable Long id) {
        return ResponseEntity.ok(service.dispatchOrder(id));
    }

    @PostMapping("/{id}/invoice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(service.generateInvoice(id));
    }

    @GetMapping("/invoices")
    public List<Invoice> getAllInvoices() { return service.getAllInvoices(); }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(service.getInvoiceById(id));
    }
}
