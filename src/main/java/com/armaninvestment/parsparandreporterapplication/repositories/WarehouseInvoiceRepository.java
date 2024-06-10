package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.WarehouseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface WarehouseInvoiceRepository extends JpaRepository<WarehouseInvoice, Long>, JpaSpecificationExecutor<WarehouseInvoice> {
    @Transactional
    @Modifying
    @Query("delete from WarehouseInvoice w where w.warehouseReceipt.id = :id")
    void deleteByReceiptId(@Param("id") Long id);

    @Query("select w from WarehouseInvoice w where w.warehouseReceipt = :id")
    Optional<WarehouseInvoice> findWarehouseInvoiceByInvoiceId(@Param("id") Long id);

    @Query("select w from WarehouseInvoice w where w.warehouseReceipt.id = ?1")
    WarehouseInvoice findWarehouseInvoiceByReceiptId(Long receiptId);
}