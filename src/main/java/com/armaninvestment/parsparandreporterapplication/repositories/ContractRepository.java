package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {
    @Query("select (count(c) > 0) from Contract c where c.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    @Query("select (count(c) > 0) from Contract c where c.year.id = :yearId")
    boolean existsByYearId(@Param("yearId") Long yearId);

    @Query("select (count(c) > 0) from Contract c where c.contractNumber = :contractNumber")
    boolean existsByContractNumber(@Param("contractNumber") String contractNumber);

    @Query("select (count(c) > 0) from Contract c where c.contractNumber = :contractNumber and c.id <> :Id")
    boolean existsByContractNumberAndIdNot(@Param("contractNumber") String contractNumber, @Param("Id") Long Id);
}