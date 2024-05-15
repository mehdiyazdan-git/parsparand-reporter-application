package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long>, JpaSpecificationExecutor<InvoiceItem> {
    @Query("select (count(i) > 0) from InvoiceItem i where i.warehouseReceipt.id = :warehouseReceiptId")
    boolean existsByWarehouseReceiptId(@Param("warehouseReceiptId") Long warehouseReceiptId);

    @Query("select (count(i) > 0) from InvoiceItem i where i.warehouseReceipt.id = :warehouseReceiptId and i.id <> :id")
    boolean existsByWarehouseReceiptIdAndIdNot(@Param("warehouseReceiptId") Long warehouseReceiptId, @Param("id") Long id);

}