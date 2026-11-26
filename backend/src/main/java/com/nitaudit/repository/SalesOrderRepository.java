package com.nitaudit.repository;

import com.nitaudit.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findBySalesmanId(Long salesmanId);
    List<SalesOrder> findByStatus(SalesOrder.Status status);
}
