package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    @Query(value = """
            SELECT i.id, i.description
                    FROM (SELECT iic.id,
                                 CONCAT('فاکتور ', iic.invoiceNumber, ' - ', iic.customerName, ' تاریخ ', iic.invoiceDate, ' تعداد',
                                        iic.quantity) AS description
                          FROM (SELECT i.id,
                                       i.invoice_number                      AS invoiceNumber,
                                       gregorian_to_persian(i.issued_date) AS invoiceDate,
                                       c.name                                          AS customerName,
                                       SUM(ii.quantity)                               AS quantity
                                FROM invoice i
                                         JOIN invoice_item ii ON i.id = ii.invoice_id
                                         JOIN customer c ON c.id = i.customer_id
                                WHERE i.jalali_year is null or i.jalali_year = :jalaliYear
                                GROUP BY i.id, invoiceNumber, invoiceDate, customerName) iic) i
                    WHERE i.description is null or i.description ILIKE '%' || :searchQuery || '%';
            """, nativeQuery = true)
    List<Object[]> searchInvoiceByDescriptionKeywords(String searchQuery,Integer jalaliYear);

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

    @Query("select i from Invoice i where i.invoiceNumber = :invoiceId")
    Optional<Invoice> findByInvoiceNumber(@Param("invoiceId") Long invoiceId);
}