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

    @Query(value = "SELECT * FROM get_monthly_report_by_year_and_month(:year, :month, cast(:productType as text))", nativeQuery = true)
    List<Object[]> getReport(@Param("year") int year, @Param("month") int month, @Param("productType") String productType);

    @Query(value = "select * from get_sales_by_year_group_by_month_filter_by_product_type(CAST(:yearId AS smallint),CAST(:productType AS text))", nativeQuery = true)
    List<Object[]> getSalesByYearGroupByMonth(@Param("yearId") Short yearId, @Param("productType") String productType);
}