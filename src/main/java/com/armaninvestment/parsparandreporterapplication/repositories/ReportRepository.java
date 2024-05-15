package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    @Query("select (count(r) > 0) from Report r where r.reportDate = :reportDate")
    boolean existsByReportDate(@Param("reportDate") LocalDate reportDate);

    @Query("select (count(r) > 0) from Report r where r.reportDate = :reportDate and r.id <> :id")
    boolean existsByReportDateAndIdNot(@Param("reportDate") LocalDate reportDate, @Param("id") Long id);

    @Query("select (count(r) > 0) from Report r where r.year.id = :yearId")
    boolean existsByYearId(@Param("yearId") Long yearId);
}