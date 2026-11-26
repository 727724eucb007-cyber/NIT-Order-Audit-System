package com.nitaudit.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaMigration {

    private final DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateStatusColumn() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String product = meta.getDatabaseProductName();
            if (!product.toLowerCase().contains("mysql")) {
                return; // skip for H2 / other test databases
            }
            conn.createStatement().execute(
                "ALTER TABLE sales_orders MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'"
            );
            log.info("Schema migration: sales_orders.status widened to VARCHAR(20)");
        } catch (Exception e) {
            // Column already VARCHAR — safe to ignore duplicate-run errors
            log.debug("Schema migration skipped or already applied: {}", e.getMessage());
        }
    }
}
