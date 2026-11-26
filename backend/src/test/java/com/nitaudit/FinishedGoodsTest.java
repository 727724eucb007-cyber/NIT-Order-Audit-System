package com.nitaudit;

import com.nitaudit.model.*;
import com.nitaudit.repository.*;
import com.nitaudit.service.FinishedGoodsService;
import com.nitaudit.service.ProductionService;
import com.nitaudit.service.SalesOrderService;
import com.nitaudit.service.StockShortageException;
import com.nitaudit.dto.StockShortageResponse;
import com.nitaudit.dto.ProductionOrderRequest;
import com.nitaudit.dto.SalesOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Transactional
class FinishedGoodsTest {

    @Autowired ProductRepository productRepo;
    @Autowired RawMaterialRepository rmRepo;
    @Autowired UserRepository userRepo;
    @Autowired CustomerRepository customerRepo;
    @Autowired BomRepository bomRepo;
    @Autowired ProductionOrderRepository productionOrderRepo;
    @Autowired SalesOrderRepository salesOrderRepo;
    @Autowired InvoiceRepository invoiceRepo;
    @Autowired FinishedGoodsLedgerRepository fgLedgerRepo;
    @Autowired FinishedGoodsService fgService;
    @Autowired ProductionService productionService;
    @Autowired SalesOrderService salesOrderService;

    Product product;
    RawMaterial rm;
    User adminUser;
    User salesUser;
    Customer customer;

    @BeforeEach
    void setup() {
        product = new Product();
        product.setName("Test Product"); product.setSku("TP-001");
        product.setUnit("pcs"); product.setSellingPrice(new BigDecimal("100"));
        product.setGstRate(new BigDecimal("12")); product.setFinishedStock(BigDecimal.ZERO);
        product = productRepo.save(product);

        rm = new RawMaterial();
        rm.setName("Test RM"); rm.setUnit("kg");
        rm.setCurrentStock(new BigDecimal("1000")); rm.setReorderLevel(BigDecimal.ZERO);
        rm.setCostPerUnit(new BigDecimal("10"));
        rm = rmRepo.save(rm);

        Bom bom = new Bom();
        bom.setProduct(product); bom.setRawMaterial(rm); bom.setQtyRequired(new BigDecimal("1"));
        bomRepo.save(bom);

        adminUser = new User();
        adminUser.setUsername("testadmin"); adminUser.setPassword("x");
        adminUser.setRole(User.Role.ADMIN); adminUser.setFullName("Test Admin");
        adminUser = userRepo.save(adminUser);

        salesUser = new User();
        salesUser.setUsername("testsales"); salesUser.setPassword("x");
        salesUser.setRole(User.Role.SALESMAN); salesUser.setFullName("Test Sales");
        salesUser = userRepo.save(salesUser);

        customer = new Customer();
        customer.setName("Test Customer"); customer.setGstin("GSTIN123");
        customer.setAddress("Addr"); customer.setPhone("1234567890"); customer.setEmail("t@t.com");
        customer = customerRepo.save(customer);
    }

    @Test
    void productionComplete_creditsFinishedGoodsStock() {
        ProductionOrderRequest req = new ProductionOrderRequest();
        req.setProductId(product.getId());
        req.setPlannedQty(new BigDecimal("100"));
        req.setNotes("test");
        ProductionOrder order = productionService.createOrder(req, adminUser.getId());
        productionService.startOrder(order.getId());

        ProductionOrderRequest completeReq = new ProductionOrderRequest();
        completeReq.setActualQty(new BigDecimal("95"));
        ProductionOrderRequest.ConsumptionEntry entry = new ProductionOrderRequest.ConsumptionEntry();
        entry.setRawMaterialId(rm.getId());
        entry.setActualQty(new BigDecimal("95"));
        completeReq.setConsumptions(List.of(entry));

        productionService.completeOrder(order.getId(), completeReq);

        Product updated = productRepo.findById(product.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("95").compareTo(updated.getFinishedStock()));
    }

    // --- Stock availability validation tests ---

    @Test
    void distributorB_approveBlocked_120required_88available_shortage32() {
        // Seed exactly 88 units of finished goods stock
        product = productRepo.findById(product.getId()).orElseThrow();
        fgService.credit(product, new BigDecimal("88"), FinishedGoodsLedger.TxnType.OPENING, "Distributor B Test");

        SalesOrderRequest req = new SalesOrderRequest();
        req.setCustomerId(customer.getId());
        req.setNotes("Distributor B order");
        SalesOrderRequest.ItemEntry item = new SalesOrderRequest.ItemEntry();
        item.setProductId(product.getId());
        item.setQty(new BigDecimal("120"));
        item.setUnitPrice(new BigDecimal("100"));
        item.setGstRate(new BigDecimal("12"));
        req.setItems(List.of(item));

        SalesOrder order = salesOrderService.createOrder(req, salesUser.getId());

        StockShortageException ex = assertThrows(StockShortageException.class,
                () -> salesOrderService.approveOrder(order.getId()));

        assertEquals(1, ex.getShortages().size());
        StockShortageResponse.ShortageItem shortage = ex.getShortages().get(0);
        assertEquals(0, new BigDecimal("120").compareTo(shortage.getRequired()));
        assertEquals(0, new BigDecimal("88").compareTo(shortage.getAvailable()));
        assertEquals(0, new BigDecimal("32").compareTo(shortage.getShortage()));

        // Order must remain PENDING
        SalesOrder unchanged = salesOrderRepo.findById(order.getId()).orElseThrow();
        assertEquals(SalesOrder.Status.PENDING, unchanged.getStatus());

        // Stock must be untouched
        Product p = productRepo.findById(product.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("88").compareTo(p.getFinishedStock()));
    }

    @Test
    void approve_sufficientStock_succeeds() {
        product = productRepo.findById(product.getId()).orElseThrow();
        fgService.credit(product, new BigDecimal("200"), FinishedGoodsLedger.TxnType.OPENING, "Sufficient stock");

        SalesOrderRequest req = new SalesOrderRequest();
        req.setCustomerId(customer.getId());
        req.setNotes("test");
        SalesOrderRequest.ItemEntry item = new SalesOrderRequest.ItemEntry();
        item.setProductId(product.getId());
        item.setQty(new BigDecimal("120"));
        item.setUnitPrice(new BigDecimal("100"));
        item.setGstRate(new BigDecimal("12"));
        req.setItems(List.of(item));

        SalesOrder order = salesOrderService.createOrder(req, salesUser.getId());
        SalesOrder approved = salesOrderService.approveOrder(order.getId());
        assertEquals(SalesOrder.Status.APPROVED, approved.getStatus());
    }

    // --- Dispatch tests ---

    @Test
    void dispatch_deductsFinishedGoodsStock() {
        // Seed 200 units of FG stock
        product = productRepo.findById(product.getId()).orElseThrow();
        fgService.credit(product, new BigDecimal("200"), FinishedGoodsLedger.TxnType.OPENING, "Test Opening");

        // Create, approve, invoice a sales order for 50 units
        SalesOrderRequest req = new SalesOrderRequest();
        req.setCustomerId(customer.getId());
        req.setNotes("test");
        SalesOrderRequest.ItemEntry item = new SalesOrderRequest.ItemEntry();
        item.setProductId(product.getId());
        item.setQty(new BigDecimal("50"));
        item.setUnitPrice(new BigDecimal("100"));
        item.setGstRate(new BigDecimal("12"));
        req.setItems(List.of(item));

        SalesOrder order = salesOrderService.createOrder(req, salesUser.getId());
        salesOrderService.approveOrder(order.getId());
        salesOrderService.generateInvoice(order.getId());
        salesOrderService.dispatchOrder(order.getId());

        Product updated = productRepo.findById(product.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("150").compareTo(updated.getFinishedStock()));

        SalesOrder dispatched = salesOrderRepo.findById(order.getId()).orElseThrow();
        assertEquals(SalesOrder.Status.DISPATCHED, dispatched.getStatus());
    }

    @Test
    void dispatch_insufficientStock_throwsException() {
        // Only 10 units in stock — approve will now be blocked too, so seed enough to approve
        // but then drain stock before dispatch to simulate the dispatch-level guard
        product = productRepo.findById(product.getId()).orElseThrow();
        fgService.credit(product, new BigDecimal("50"), FinishedGoodsLedger.TxnType.OPENING, "Test Opening");

        SalesOrderRequest req = new SalesOrderRequest();
        req.setCustomerId(customer.getId());
        req.setNotes("test");
        SalesOrderRequest.ItemEntry item = new SalesOrderRequest.ItemEntry();
        item.setProductId(product.getId());
        item.setQty(new BigDecimal("50"));
        item.setUnitPrice(new BigDecimal("100"));
        item.setGstRate(new BigDecimal("12"));
        req.setItems(List.of(item));

        SalesOrder order = salesOrderService.createOrder(req, salesUser.getId());
        salesOrderService.approveOrder(order.getId());
        salesOrderService.generateInvoice(order.getId());

        // Drain stock so dispatch fails
        product = productRepo.findById(product.getId()).orElseThrow();
        product.setFinishedStock(new BigDecimal("10"));
        productRepo.save(product);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> salesOrderService.dispatchOrder(order.getId()));
        assertTrue(ex.getMessage().contains("Insufficient finished-goods stock"));
    }

    @Test
    void dispatch_duplicate_throwsException() {
        product = productRepo.findById(product.getId()).orElseThrow();
        fgService.credit(product, new BigDecimal("200"), FinishedGoodsLedger.TxnType.OPENING, "Test Opening");

        SalesOrderRequest req = new SalesOrderRequest();
        req.setCustomerId(customer.getId());
        req.setNotes("test");
        SalesOrderRequest.ItemEntry item = new SalesOrderRequest.ItemEntry();
        item.setProductId(product.getId());
        item.setQty(new BigDecimal("50"));
        item.setUnitPrice(new BigDecimal("100"));
        item.setGstRate(new BigDecimal("12"));
        req.setItems(List.of(item));

        SalesOrder order = salesOrderService.createOrder(req, salesUser.getId());
        salesOrderService.approveOrder(order.getId());
        salesOrderService.generateInvoice(order.getId());
        salesOrderService.dispatchOrder(order.getId());

        // Second dispatch must fail -- status is now DISPATCHED, not INVOICED
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> salesOrderService.dispatchOrder(order.getId()));
        assertTrue(ex.getMessage().contains("INVOICED"));
    }
}
