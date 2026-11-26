
package com.nitaudit.service;

import com.nitaudit.dto.SalesOrderRequest;
import com.nitaudit.dto.StockShortageResponse;
import com.nitaudit.model.*;
import com.nitaudit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final InvoiceRepository invoiceRepo;
    private final FinishedGoodsService fgService;

    @Transactional
    public SalesOrder createOrder(SalesOrderRequest req, Long salesmanId) {

        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        User salesman = userRepo.findById(salesmanId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setSalesman(salesman);
        order.setNotes(req.getNotes());

        BigDecimal total = BigDecimal.ZERO;

        for (SalesOrderRequest.ItemEntry ie : req.getItems()) {

            Product product = productRepo.findById(ie.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found: " + ie.getProductId()));

            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrder(order);
            item.setProduct(product);
            item.setQty(ie.getQty());
            item.setUnitPrice(
                    ie.getUnitPrice() != null
                            ? ie.getUnitPrice()
                            : product.getSellingPrice());

            item.setGstRate(
                    ie.getGstRate() != null
                            ? ie.getGstRate()
                            : product.getGstRate());

            order.getItems().add(item);

            BigDecimal lineTotal =
                    item.getUnitPrice().multiply(item.getQty());

            BigDecimal gstAmt = lineTotal
                    .multiply(item.getGstRate())
                    .divide(BigDecimal.valueOf(100));

            total = total.add(lineTotal).add(gstAmt);
        }

        order.setTotalAmount(total);

        return orderRepo.save(order);
    }

    @Transactional
    public SalesOrder approveOrder(Long orderId) {

        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != SalesOrder.Status.PENDING) {
            throw new RuntimeException("Order is not pending");
        }

        System.out.println("STOCK CHECK: Order ID=" + orderId);

        List<StockShortageResponse.ShortageItem> shortages =
                new ArrayList<>();

        for (SalesOrderItem item : order.getItems()) {

            Product product = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            BigDecimal available = product.getFinishedStock() != null
                    ? product.getFinishedStock()
                    : BigDecimal.ZERO;

            System.out.println(
                    "Product: " + product.getName()
                    + " | Required: " + item.getQty()
                    + " | Available: " + available
            );

            if (available.compareTo(item.getQty()) < 0) {

                shortages.add(
                        new StockShortageResponse.ShortageItem(
                                product.getName(),
                                item.getQty(),
                                available,
                                item.getQty().subtract(available)
                        )
                );
            }
        }

        if (!shortages.isEmpty()) {
            throw new StockShortageException(shortages);
        }

        order.setStatus(SalesOrder.Status.APPROVED);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepo.save(order);
    }

    @Transactional
    public SalesOrder rejectOrder(Long orderId, String reason) {

        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(SalesOrder.Status.REJECTED);
        order.setNotes(reason);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepo.save(order);
    }

    @Transactional
    public Invoice generateInvoice(Long orderId) {

        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != SalesOrder.Status.APPROVED) {
            throw new RuntimeException(
                    "Order must be APPROVED to generate invoice");
        }

        invoiceRepo.findBySalesOrderId(orderId).ifPresent(i -> {
            throw new RuntimeException(
                    "Invoice already exists: " + i.getInvoiceNumber());
        });

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;

        for (SalesOrderItem item : order.getItems()) {

            BigDecimal lineTotal =
                    item.getUnitPrice().multiply(item.getQty());

            BigDecimal gstAmt = lineTotal
                    .multiply(item.getGstRate())
                    .divide(BigDecimal.valueOf(100));

            subtotal = subtotal.add(lineTotal);
            gstTotal = gstTotal.add(gstAmt);
        }

        long count = invoiceRepo.count() + 1;

        String invNum = "INV-"
                + LocalDate.now().format(
                        DateTimeFormatter.ofPattern("yyyyMM"))
                + "-"
                + String.format("%04d", count);

        Invoice invoice = new Invoice();
        invoice.setSalesOrder(order);
        invoice.setInvoiceNumber(invNum);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setSubtotal(subtotal);
        invoice.setGstAmount(gstTotal);
        invoice.setTotalAmount(subtotal.add(gstTotal));

        order.setStatus(SalesOrder.Status.INVOICED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepo.save(order);

        return invoiceRepo.save(invoice);
    }

    @Transactional
    public SalesOrder dispatchOrder(Long orderId) {

        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != SalesOrder.Status.INVOICED) {
            throw new RuntimeException(
                    "Order must be INVOICED before dispatch");
        }

        for (SalesOrderItem item : order.getItems()) {

            Product product = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            fgService.debit(
                    product,
                    item.getQty(),
                    "Sales Order #" + orderId
            );
        }

        order.setStatus(SalesOrder.Status.DISPATCHED);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepo.save(order);
    }

    public List<SalesOrder> getAll() {
        return orderRepo.findAll();
    }

    public List<SalesOrder> getBySalesman(Long id) {
        return orderRepo.findBySalesmanId(id);
    }

    public SalesOrder getById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepo.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }
}
