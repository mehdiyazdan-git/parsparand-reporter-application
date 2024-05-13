package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Establishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, Long>, JpaSpecificationExecutor<Establishment> {
    @Query("select (count(e) > 0) from Establishment e where e.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);
}