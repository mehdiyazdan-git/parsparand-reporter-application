package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    @Query("select (count(r) > 0) from Report r where r.reportDate = :reportDate")
    boolean existsByReportDate(@Param("reportDate") LocalDate reportDate);

    @Query("select (count(r) > 0) from Report r where r.reportDate = :reportDate and r.id <> :id")
    boolean existsByReportDateAndIdNot(@Param("reportDate") LocalDate reportDate, @Param("id") Long id);

    @Query("select (count(r) > 0) from Report r where r.year.id = :yearId")
    boolean existsByYearId(@Param("yearId") Long yearId);

    @Query(value = "SELECT * FROM get_monthly_report_by_year_and_month(:year, :month, cast(:productType as integer))", nativeQuery = true)
    List<Object[]> getReport(@Param("year") int year, @Param("month") int month, @Param("productType") int productType);

    @Query(value = "select * from get_sales_by_year_group_by_month_filter_by_product_type(CAST(:yearId AS integer),CAST(:productType AS integer))", nativeQuery = true)
    List<Object[]> getSalesByYearGroupByMonth(@Param("yearId") Short yearId, @Param("productType") int productType);

    @Query(nativeQuery = true,value = """

            WITH BigCustomers AS (
                                  SELECT
                                      c.id AS customerId,
                                      c.name AS customerName
                                  FROM customer c
                                  WHERE c.big_customer = true
                              ),
                                   AllCustomers AS (
                                       SELECT customerName FROM BigCustomers
                                       UNION
                                       SELECT 'سایر' AS customerName
                                   ),
                                   MonthlySales AS (
                                       SELECT
                                           COALESCE(bc.customerName, 'سایر') AS customerName,
                                           COALESCE(SUM(wri.quantity * wri.unit_price), 0) AS totalAmount,
                                           COALESCE(SUM(wri.quantity), 0) AS totalQuantity,
                                           r.jalali_year,
                                           r.month,
                                           p.product_type
                                       FROM customer c
                                                LEFT JOIN warehouse_receipt wr ON wr.customer_id = c.id
                                                LEFT JOIN report_item ri ON ri.warehouse_receipt_id = wr.id
                                                LEFT JOIN warehouse_receipt_item wri ON wri.warehouse_receipt_id = wr.id
                                                LEFT JOIN product p ON wri.product_id = p.id
                                                LEFT JOIN report r ON ri.report_id = r.id
                                                LEFT JOIN BigCustomers bc ON c.id = bc.customerId
                                       WHERE r.jalali_year = $1 AND r.month <= $2 AND p.product_type = $3
                                       GROUP BY COALESCE(bc.customerName, 'سایر'), r.jalali_year, r.month, p.product_type
                                   ),
                                   CumulativeSales AS (
                                       SELECT
                                           ms.customerName,
                                           SUM(ms.totalAmount) OVER (PARTITION BY ms.customerName, ms.product_type ORDER BY ms.jalali_year, ms.month) AS cumulativeTotalAmount,
                                           SUM(ms.totalQuantity) OVER (PARTITION BY ms.customerName, ms.product_type ORDER BY ms.jalali_year, ms.month) AS cumulativeTotalQuantity,
                                           ms.jalali_year,
                                           ms.month,
                                           ms.product_type,
                                           ms.totalAmount,
                                           ms.totalQuantity
                                       FROM MonthlySales ms
                                   ),
                                   TotalSales AS (
                                       SELECT
                                           SUM(totalAmount) AS totalSales
                                       FROM CumulativeSales
                                       WHERE month = :month AND jalali_year = :jalaliYear
                                   )
                              SELECT
                                  ac.customerName,
                                  COALESCE(cs.totalAmount, 0) AS totalAmount,
                                  COALESCE(cs.totalQuantity, 0) AS totalQuantity,
                                  COALESCE(cs.cumulativeTotalQuantity, 0) AS cumulativeTotalQuantity,
                                  COALESCE(cs.cumulativeTotalAmount, 0) AS cumulativeTotalAmount,
                                  ROUND(COALESCE(cs.totalAmount, 0)::numeric / NULLIF(cs.totalQuantity, 0), 2) AS avgUnitPrice,
                                  ROUND(COALESCE(cs.totalAmount, 0)::numeric / (SELECT totalSales FROM TotalSales) * 100, 2) AS relativePercentage
                              FROM AllCustomers ac
                                       LEFT JOIN CumulativeSales cs ON ac.customerName = cs.customerName AND cs.month = :month AND cs.jalali_year = :jalaliYear
                              ORDER BY ac.customerName;
    """)
    List<Object[]> getSalesByYearGroupByMonthFilterByProductType(
            @Param("jalaliYear") Integer jalaliYear,
            @Param("month") Integer month,
            @Param("productType") Integer productType
    );
}