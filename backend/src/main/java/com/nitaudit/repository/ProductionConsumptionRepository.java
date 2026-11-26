package com.nitaudit.repository;

import com.nitaudit.model.ProductionConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionConsumptionRepository extends JpaRepository<ProductionConsumption, Long> {
    List<ProductionConsumption> findByProductionOrderId(Long orderId);
}
