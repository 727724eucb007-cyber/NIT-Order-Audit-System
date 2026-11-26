package com.nitaudit.repository;

import com.nitaudit.model.Bom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BomRepository extends JpaRepository<Bom, Long> {
    List<Bom> findByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM Bom b WHERE b.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
