package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("select (count(p) > 0) from Product p where p.productName = :name")
    boolean existsByProductName(@Param("name") String name);

    @Query("select (count(p) > 0) from Product p where p.productName = :name and p.id <> :id")
    boolean existsByProductNameAndAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("select (count(p) > 0) from Product p where p.productCode = :code")
    boolean existsByProductCode(@Param("code") String code);

    @Query("select (count(p) > 0) from Product p where p.productCode = :code and p.id <> :id")
    boolean existsByProductCodeAndAndIdNot(@Param("code") String code, @Param("id") Long id);
}