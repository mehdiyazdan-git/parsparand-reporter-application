package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceipt, Long>, JpaSpecificationExecutor<WarehouseReceipt> {

    @Query("""
            select w from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber and w.warehouseReceiptDate = :warehouseReceiptDate""")
    Optional<WarehouseReceipt> findByWarehouseReceiptNumberAndWarehouseReceiptDate(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber, @Param("warehouseReceiptDate") LocalDate warehouseReceiptDate);
    @Query("""
            select (count(w) > 0) from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber and w.year.id = :yearId""")
    boolean existsByWarehouseReceiptNumberAndYearId(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber, @Param("yearId") Long yearId);
    @Query("""
        select (count(w) > 0) from WarehouseReceipt w
        where w.warehouseReceiptNumber = :warehouseReceiptNumber and w.year.id = :yearId and w.id <> :id""")
    boolean existsByWarehouseReceiptNumberAndYearIdAndIdNot(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber, @Param("yearId") Long yearId, @Param("id") Long id);


    @Query("select (count(w) > 0) from WarehouseReceipt w where w.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    @Query("select (count(w) > 0) from WarehouseReceipt w where w.year.id = :yearId")
    boolean existsByYearId(@Param("yearId") Long yearId);

    @Query(nativeQuery = true, value = """
        SELECT i.id, i.description
        FROM (
            SELECT wwc.id,
                   CONCAT('حواله ', wwc.receiptNo, '-', wwc.customerName, ' تاریخ ', wwc.receiptDate, ' تعداد ', wwc.quantity) AS description
            FROM (
                SELECT wr.id,
                       wr.warehouse_receipt_number AS receiptNo,
                       gregorian_to_persian(wr.warehouse_receipt_date) AS receiptDate,
                       c.name AS customerName,
                       SUM(wri.quantity) AS quantity
                FROM warehouse_receipt wr
                JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
                JOIN customer c ON c.id = wr.customer_id
                WHERE :fiscalYearId IS NULL OR wr.year_id = :fiscalYearId
                GROUP BY wr.id, wr.warehouse_receipt_number, wr.warehouse_receipt_date, c.name
            ) wwc
        ) i
        WHERE :searchTerm IS NULL OR i.description ILIKE '%' || :searchTerm || '%'
    """)
    List<Object[]> searchWarehouseReceiptByDescriptionKeywords(
            @Param("searchTerm") String searchTerm,
            @Param("fiscalYearId") Long fiscalYearId
    );


    @Query(nativeQuery = true,value = """
            select
             cast(coalesce(sum(wri.quantity * wri.unit_price),0) as double precision),
             cast(coalesce(sum(wri.quantity),0) as double precision)
              from  warehouse_invoice\s
            join warehouse_receipt wr on warehouse_invoice.receipt_id = wr.id \s
            join warehouse_receipt_item wri on wr.id = wri.warehouse_receipt_id \s
            where warehouse_invoice.invoice_id is null\s
            and customer_id = :customerId\s""")
    List<Object[]> getNotInvoicedAmountByCustomerId(Long customerId);

}