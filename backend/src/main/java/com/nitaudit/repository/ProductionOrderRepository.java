package com.nitaudit.repository;

import com.nitaudit.model.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    List<ProductionOrder> findByCreatedById(Long userId);
}
