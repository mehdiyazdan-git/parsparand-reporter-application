package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    @Query("select c from Customer c where c.customerCode = :customerCode")
    Optional<Customer> findByCustomerCode(@Param("customerCode") @Size(max = 255) String customerCode);
    @Query("select (count(c) > 0) from Customer c where c.name = :name")
    boolean existsByName(@Param("name") String name);

    @Query("select (count(c) > 0) from Customer c where c.name = :name and c.id <> :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("select (count(c) > 0) from Customer c where c.customerCode = :customerCode")
    boolean existsByCustomerCode(@Param("customerCode") String customerCode);

    @Query("select (count(c) > 0) from Customer c where c.customerCode = :customerCode and c.id <> :id")
    boolean existsByCustomerCodeAndIdNot(@Param("customerCode") String customerCode, @Param("id") Long id);
}