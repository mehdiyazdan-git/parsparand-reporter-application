package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnedRepository extends JpaRepository<Returned, Long>, JpaSpecificationExecutor<Returned> {
    @Query("select (count(r) > 0) from Returned r where r.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);
}