package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.searchForms.CustomerSearch;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {

    /**
     * Creates a specification based on the provided CustomerSearch criteria.
     * @param searchCriteria the criteria for filtering customers
     * @return a Specification for the Customer entity
     */
    public static Specification<Customer> bySearchCriteria(CustomerSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getName() != null && !searchCriteria.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + searchCriteria.getName() + "%"));
            }
            if (searchCriteria.getPhone() != null && !searchCriteria.getPhone().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("phone"), "%" + searchCriteria.getPhone() + "%"));
            }
            if (searchCriteria.getCustomerCode() != null && !searchCriteria.getCustomerCode().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("customerCode"), "%" + searchCriteria.getCustomerCode() + "%"));
            }
            if (searchCriteria.getEconomicCode() != null && !searchCriteria.getEconomicCode().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("economicCode"), "%" + searchCriteria.getEconomicCode() + "%"));
            }
            if (searchCriteria.getNationalCode() != null && !searchCriteria.getNationalCode().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("nationalCode"), "%" + searchCriteria.getNationalCode() + "%"));
            }
            if (Boolean.TRUE.equals(searchCriteria.getBigCustomer())) { // Handle boolean check correctly
                predicates.add(criteriaBuilder.isTrue(root.get("bigCustomer")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Customer> getSelectSpecification(String searchParam){
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (searchParam !=null && !searchParam.isEmpty()) {
                predicate =(cb.like(root.get("name"), "%" + searchParam.trim() + "%"));
            }
            return predicate;
        };
    }
}
