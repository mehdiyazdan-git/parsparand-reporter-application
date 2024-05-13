package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {


    @Query("select (count(i) > 0) from Invoice i where i.invoiceNumber = :invoiceNumber and i.year.id = :year_id")
    boolean existsByInvoiceNumberAndYearId(@Param("invoiceNumber") Long invoiceNumber, @Param("year_id") Long year_id);

    @Query("""
            select (count(i) > 0) from Invoice i
            where i.invoiceNumber = :invoiceNumber and i.year.id = :year_id and i.id <> :id""")
    boolean existsByInvoiceNumberAndYearIdAndIdNot(@Param("invoiceNumber") Long invoiceNumber, @Param("year_id") Long year_id, @Param("id") Long id);


    @Query("select (count(i) > 0) from Invoice i where i.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    @Query("select (count(i) > 0) from Invoice i where i.contract.id = :contractId")
    boolean existsByContractId(@Param("contractId") Long contractId);

    @Query("select (count(i) > 0) from Invoice i where i.year.id = :yearId")
    boolean existsByYearId(@Param("yearId") Long yearId);
}