package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.VATRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface VATRateRepository extends JpaRepository<VATRate, Long>, JpaSpecificationExecutor<VATRate> {

  @Query("SELECT v FROM VATRate v WHERE v.effectiveFrom <= :date AND (v.effectiveTo IS NULL OR v.effectiveTo >= :date)")
  Optional<VATRate> findByEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(@Param("date") LocalDate date);

}