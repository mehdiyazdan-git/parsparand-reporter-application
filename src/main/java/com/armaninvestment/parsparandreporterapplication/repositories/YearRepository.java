package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface YearRepository extends JpaRepository<Year, Long>, JpaSpecificationExecutor<Year> {
}