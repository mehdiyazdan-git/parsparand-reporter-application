package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    @Query("select (count(p) > 0) from Payment p where p.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    @Query("select (count(p) > 0) from Payment p where p.year.id = :yearId")
    boolean existsByYearId(@Param("yearId") Long yearId);
}