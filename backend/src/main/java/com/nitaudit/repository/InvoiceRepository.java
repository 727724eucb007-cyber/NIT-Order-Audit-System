package com.nitaudit.repository;

import com.nitaudit.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findBySalesOrderId(Long salesOrderId);
    long countBy();
}
