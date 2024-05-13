package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceipt, Long>, JpaSpecificationExecutor<WarehouseReceipt> {
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
}