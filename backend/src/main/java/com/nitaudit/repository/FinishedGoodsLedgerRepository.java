package com.nitaudit.repository;

import com.nitaudit.model.FinishedGoodsLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinishedGoodsLedgerRepository extends JpaRepository<FinishedGoodsLedger, Long> {
    List<FinishedGoodsLedger> findByProductIdOrderByTxnDateDesc(Long productId);
    List<FinishedGoodsLedger> findAllByOrderByTxnDateDesc();
    boolean existsByProductId(Long productId);
}
