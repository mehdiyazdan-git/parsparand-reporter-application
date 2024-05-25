package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.Contract;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.searchForms.ContractSearch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ContractSpecification {

    public static Specification<Contract> bySearchCriteria(ContractSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getContractNumber() != null && !searchCriteria.getContractNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("contractNumber"), "%" + searchCriteria.getContractNumber() + "%"));
            }
            if (searchCriteria.getContractDescription() != null && !searchCriteria.getContractDescription().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("contractDescription"), "%" + searchCriteria.getContractDescription() + "%"));
            }
            if (searchCriteria.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), searchCriteria.getStartDate()));
            }
            if (searchCriteria.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), searchCriteria.getEndDate()));
            }
            if (searchCriteria.getAdvancePayment() != null) {
                predicates.add(criteriaBuilder.equal(root.get("advancePayment"), searchCriteria.getAdvancePayment()));
            }
            if (searchCriteria.getPerformanceBond() != null) {
                predicates.add(criteriaBuilder.equal(root.get("performanceBond"), searchCriteria.getPerformanceBond()));
            }
            if (searchCriteria.getInsuranceDeposit() != null) {
                predicates.add(criteriaBuilder.equal(root.get("insuranceDeposit"), searchCriteria.getInsuranceDeposit()));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                Join<Contract, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(customerJoin.get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getJalaliYear() != null) {
                Join<Contract, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("name"), searchCriteria.getJalaliYear()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Contract> getSelectSpecification(String searchParam){
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (searchParam !=null && !searchParam.isEmpty()) {
                predicate =(cb.like(root.get("contractDescription"), "%" + searchParam.trim() + "%"));
            }
            return predicate;
        };

    }
}
