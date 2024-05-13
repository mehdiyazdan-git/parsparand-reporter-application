package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReturnedSearch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReturnedSpecification {

    /**
     * Creates a specification based on the provided ReturnedSearch criteria.
     * @param searchCriteria the criteria for filtering returned items
     * @return a Specification for the Returned entity
     */
    public static Specification<Returned> bySearchCriteria(ReturnedSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getReturnedNumber() != null) {
                predicates.add(criteriaBuilder.equal(root.get("returnedNumber"), searchCriteria.getReturnedNumber()));
            }
            if (searchCriteria.getReturnedDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("returnedDate"), searchCriteria.getReturnedDate()));
            }
            if (searchCriteria.getReturnedDescription() != null && !searchCriteria.getReturnedDescription().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("returnedDescription"), "%" + searchCriteria.getReturnedDescription() + "%"));
            }
            if (searchCriteria.getQuantity() != null) {
                predicates.add(criteriaBuilder.equal(root.get("quantity"), searchCriteria.getQuantity()));
            }
            if (searchCriteria.getUnitPrice() != null) {
                predicates.add(criteriaBuilder.equal(root.get("unitPrice"), searchCriteria.getUnitPrice()));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                Join<Returned, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(customerJoin.get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
