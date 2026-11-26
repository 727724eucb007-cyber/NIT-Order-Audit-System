package com.nitaudit.controller;

import com.nitaudit.model.*;
import com.nitaudit.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {
    private final SalesOrderRepository salesOrderRepo;
    private final ProductionOrderRepository productionRepo;
    private final InventoryLedgerRepository ledgerRepo;
    private final ProductionConsumptionRepository consumptionRepo;

    @GetMapping("/sales-orders/csv")
    public ResponseEntity<byte[]> salesOrdersCsv() throws Exception {
        StringWriter sw = new StringWriter();
        try (CSVPrinter p = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(
                "Order ID","Customer","Salesman","Status","Total Amount","Created At"))) {
            for (SalesOrder o : salesOrderRepo.findAll()) {
                p.printRecord(o.getId(), o.getCustomer().getName(),
                        o.getSalesman().getFullName(), o.getStatus(),
                        o.getTotalAmount(), o.getCreatedAt());
            }
        }
        return csvResponse(sw.toString(), "sales_orders.csv");
    }

    @GetMapping("/inventory/csv")
    public ResponseEntity<byte[]> inventoryCsv() throws Exception {
        StringWriter sw = new StringWriter();
        try (CSVPrinter p = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(
                "ID","Raw Material","Txn Type","Qty","Balance After","Reference","Date"))) {
            for (InventoryLedger l : ledgerRepo.findAllByOrderByTxnDateDesc()) {
                p.printRecord(l.getId(), l.getRawMaterial().getName(),
                        l.getTxnType(), l.getQty(), l.getBalanceAfter(),
                        l.getReference(), l.getTxnDate());
            }
        }
        return csvResponse(sw.toString(), "inventory_ledger.csv");
    }

    @GetMapping("/production/variance/csv")
    public ResponseEntity<byte[]> varianceCsv() throws Exception {
        StringWriter sw = new StringWriter();
        try (CSVPrinter p = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(
                "Production Order ID","Product","Planned Qty","Actual Qty","Yield Variance %",
                "Raw Material","Planned Consumption","Actual Consumption","Consumption Variance"))) {
            for (ProductionOrder o : productionRepo.findAll()) {
                if (o.getStatus() != ProductionOrder.Status.COMPLETED) continue;
                BigDecimal yieldVar = BigDecimal.ZERO;
                if (o.getPlannedQty().compareTo(BigDecimal.ZERO) > 0) {
                    yieldVar = o.getActualQty().subtract(o.getPlannedQty())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(o.getPlannedQty(), 2, java.math.RoundingMode.HALF_UP);
                }
                List<ProductionConsumption> cons = consumptionRepo.findByProductionOrderId(o.getId());
                if (cons.isEmpty()) {
                    p.printRecord(o.getId(), o.getProduct().getName(),
                            o.getPlannedQty(), o.getActualQty(), yieldVar, "-", "-", "-", "-");
                } else {
                    for (ProductionConsumption c : cons) {
                        BigDecimal consVar = c.getActualQty().subtract(c.getPlannedQty());
                        p.printRecord(o.getId(), o.getProduct().getName(),
                                o.getPlannedQty(), o.getActualQty(), yieldVar,
                                c.getRawMaterial().getName(), c.getPlannedQty(),
                                c.getActualQty(), consVar);
                    }
                }
            }
        }
        return csvResponse(sw.toString(), "production_variance.csv");
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalSalesOrders", salesOrderRepo.count());
        data.put("pendingOrders", salesOrderRepo.findByStatus(SalesOrder.Status.PENDING).size());
        data.put("approvedOrders", salesOrderRepo.findByStatus(SalesOrder.Status.APPROVED).size());
        data.put("totalProductionOrders", productionRepo.count());
        data.put("completedProduction", productionRepo.findAll().stream()
                .filter(o -> o.getStatus() == ProductionOrder.Status.COMPLETED).count());
        BigDecimal totalRevenue = salesOrderRepo.findByStatus(SalesOrder.Status.INVOICED)
                .stream().map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalRevenue", totalRevenue);
        return ResponseEntity.ok(data);
    }

    private ResponseEntity<byte[]> csvResponse(String content, String filename) {
        byte[] bytes = content.getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
