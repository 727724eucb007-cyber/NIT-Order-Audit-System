package com.nitaudit.repository;

import com.nitaudit.model.InventoryLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryLedgerRepository extends JpaRepository<InventoryLedger, Long> {
    List<InventoryLedger> findByRawMaterialIdOrderByTxnDateDesc(Long rawMaterialId);
    List<InventoryLedger> findAllByOrderByTxnDateDesc();
}
