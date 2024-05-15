package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.ReportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportItemRepository extends JpaRepository<ReportItem, Long>, JpaSpecificationExecutor<ReportItem> {

    @Query("select (count(r) > 0) from ReportItem r where r.warehouseReceipt.id = :warehouseReceiptId")
    boolean existsByWarehouseReceiptId(@Param("warehouseReceiptId") Long warehouseReceiptId);

    @Query("select (count(r) > 0) from ReportItem r where r.warehouseReceipt.id = :warehouseReceiptId and r.id <> :id")
    boolean existsByWarehouseReceiptIdAndIdNot(@Param("warehouseReceiptId") Long warehouseReceiptId, @Param("id") Long id);

    @Query("select (count(r) > 0) from ReportItem r where r.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);
}