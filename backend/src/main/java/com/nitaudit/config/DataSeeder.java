package com.nitaudit.config;

import com.nitaudit.model.*;
import com.nitaudit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final RawMaterialRepository rmRepo;
    private final BomRepository bomRepo;
    private final InventoryLedgerRepository ledgerRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return;
        log.info("Seeding benchmark data...");

        // Users
        User admin = createUser("admin", "admin123", User.Role.ADMIN, "Admin User");
        User salesman1 = createUser("salesman1", "sales123", User.Role.SALESMAN, "Ravi Kumar");
        User salesman2 = createUser("salesman2", "sales123", User.Role.SALESMAN, "Priya Sharma");
        User prod1 = createUser("production1", "prod123", User.Role.PRODUCTION, "Suresh Babu");
        userRepo.saveAll(List.of(admin, salesman1, salesman2, prod1));

        // Customers
        Customer c1 = customer("FreshMart Retail Pvt Ltd", "33AABCF1234A1Z5", "12, Anna Salai, Chennai - 600002", "9876543210", "purchase@freshmart.in");
        Customer c2 = customer("GreenLeaf Distributors", "29AADCG5678B1Z3", "45, MG Road, Bengaluru - 560001", "9845012345", "orders@greenleaf.co.in");
        Customer c3 = customer("NutriChoice Foods", "27AABHN9012C1Z1", "78, FC Road, Pune - 411004", "9823456789", "procurement@nutrichoice.com");
        customerRepo.saveAll(List.of(c1, c2, c3));

        // Products
        Product p1 = product("Tomato Ketchup 500g", "TK-500", "Bottle", new BigDecimal("85.00"), new BigDecimal("12.00"));
        Product p2 = product("Mango Pickle 250g", "MP-250", "Jar", new BigDecimal("120.00"), new BigDecimal("5.00"));
        Product p3 = product("Mixed Fruit Jam 200g", "MFJ-200", "Jar", new BigDecimal("95.00"), new BigDecimal("12.00"));
        productRepo.saveAll(List.of(p1, p2, p3));

        // Raw Materials
        RawMaterial rm1 = rm("Fresh Tomatoes", "kg", new BigDecimal("500.000"), new BigDecimal("100.000"), new BigDecimal("25.00"));
        RawMaterial rm2 = rm("Sugar", "kg", new BigDecimal("200.000"), new BigDecimal("50.000"), new BigDecimal("42.00"));
        RawMaterial rm3 = rm("Vinegar", "litre", new BigDecimal("80.000"), new BigDecimal("20.000"), new BigDecimal("35.00"));
        RawMaterial rm4 = rm("Salt", "kg", new BigDecimal("150.000"), new BigDecimal("30.000"), new BigDecimal("18.00"));
        RawMaterial rm5 = rm("Raw Mangoes", "kg", new BigDecimal("300.000"), new BigDecimal("80.000"), new BigDecimal("30.00"));
        RawMaterial rm6 = rm("Mustard Oil", "litre", new BigDecimal("60.000"), new BigDecimal("15.000"), new BigDecimal("120.00"));
        RawMaterial rm7 = rm("Spice Mix", "kg", new BigDecimal("40.000"), new BigDecimal("10.000"), new BigDecimal("250.00"));
        RawMaterial rm8 = rm("Mixed Fruits", "kg", new BigDecimal("250.000"), new BigDecimal("60.000"), new BigDecimal("55.00"));
        RawMaterial rm9 = rm("Pectin", "kg", new BigDecimal("15.000"), new BigDecimal("5.000"), new BigDecimal("800.00"));
        RawMaterial rm10 = rm("Glass Bottles 500ml", "pcs", new BigDecimal("1000.000"), new BigDecimal("200.000"), new BigDecimal("8.00"));
        RawMaterial rm11 = rm("Glass Jars 250ml", "pcs", new BigDecimal("800.000"), new BigDecimal("150.000"), new BigDecimal("6.00"));
        RawMaterial rm12 = rm("Glass Jars 200ml", "pcs", new BigDecimal("600.000"), new BigDecimal("100.000"), new BigDecimal("5.50"));
        rmRepo.saveAll(List.of(rm1,rm2,rm3,rm4,rm5,rm6,rm7,rm8,rm9,rm10,rm11,rm12));

        // BOM: Tomato Ketchup 500g (per bottle)
        bomRepo.saveAll(List.of(
            bom(p1, rm1, new BigDecimal("0.8500")),   // 850g tomatoes
            bom(p1, rm2, new BigDecimal("0.1200")),   // 120g sugar
            bom(p1, rm3, new BigDecimal("0.0300")),   // 30ml vinegar
            bom(p1, rm4, new BigDecimal("0.0100")),   // 10g salt
            bom(p1, rm10, new BigDecimal("1.0000"))   // 1 bottle
        ));

        // BOM: Mango Pickle 250g (per jar)
        bomRepo.saveAll(List.of(
            bom(p2, rm5, new BigDecimal("0.2200")),   // 220g raw mango
            bom(p2, rm6, new BigDecimal("0.0250")),   // 25ml mustard oil
            bom(p2, rm7, new BigDecimal("0.0150")),   // 15g spice mix
            bom(p2, rm4, new BigDecimal("0.0080")),   // 8g salt
            bom(p2, rm11, new BigDecimal("1.0000"))   // 1 jar
        ));

        // BOM: Mixed Fruit Jam 200g (per jar)
        bomRepo.saveAll(List.of(
            bom(p3, rm8, new BigDecimal("0.1800")),   // 180g mixed fruits
            bom(p3, rm2, new BigDecimal("0.0900")),   // 90g sugar
            bom(p3, rm9, new BigDecimal("0.0020")),   // 2g pectin
            bom(p3, rm12, new BigDecimal("1.0000"))   // 1 jar
        ));

        // Seed initial ledger entries for opening stock
        seedLedger(rm1, new BigDecimal("500.000"), "Opening Stock");
        seedLedger(rm2, new BigDecimal("200.000"), "Opening Stock");
        seedLedger(rm3, new BigDecimal("80.000"), "Opening Stock");
        seedLedger(rm4, new BigDecimal("150.000"), "Opening Stock");
        seedLedger(rm5, new BigDecimal("300.000"), "Opening Stock");
        seedLedger(rm6, new BigDecimal("60.000"), "Opening Stock");
        seedLedger(rm7, new BigDecimal("40.000"), "Opening Stock");
        seedLedger(rm8, new BigDecimal("250.000"), "Opening Stock");
        seedLedger(rm9, new BigDecimal("15.000"), "Opening Stock");
        seedLedger(rm10, new BigDecimal("1000.000"), "Opening Stock");
        seedLedger(rm11, new BigDecimal("800.000"), "Opening Stock");
        seedLedger(rm12, new BigDecimal("600.000"), "Opening Stock");

        log.info("Benchmark data seeded successfully.");
    }

    private User createUser(String username, String password, User.Role role, String fullName) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        u.setFullName(fullName);
        return u;
    }

    private Customer customer(String name, String gstin, String address, String phone, String email) {
        Customer c = new Customer();
        c.setName(name); c.setGstin(gstin); c.setAddress(address);
        c.setPhone(phone); c.setEmail(email);
        return c;
    }

    private Product product(String name, String sku, String unit, BigDecimal price, BigDecimal gst) {
        Product p = new Product();
        p.setName(name); p.setSku(sku); p.setUnit(unit);
        p.setSellingPrice(price); p.setGstRate(gst);
        return p;
    }

    private RawMaterial rm(String name, String unit, BigDecimal stock, BigDecimal reorder, BigDecimal cost) {
        RawMaterial r = new RawMaterial();
        r.setName(name); r.setUnit(unit); r.setCurrentStock(stock);
        r.setReorderLevel(reorder); r.setCostPerUnit(cost);
        return r;
    }

    private Bom bom(Product p, RawMaterial rm, BigDecimal qty) {
        Bom b = new Bom();
        b.setProduct(p); b.setRawMaterial(rm); b.setQtyRequired(qty);
        return b;
    }

    private void seedLedger(RawMaterial rm, BigDecimal qty, String ref) {
        InventoryLedger l = new InventoryLedger();
        l.setRawMaterial(rm);
        l.setTxnType(InventoryLedger.TxnType.PURCHASE);
        l.setQty(qty);
        l.setBalanceAfter(qty);
        l.setReference(ref);
        ledgerRepo.save(l);
    }
}
