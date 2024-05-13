package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.Payment;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.searchForms.PaymentSearch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    /**
     * Creates a specification based on the provided PaymentSearch criteria.
     * @param searchCriteria the criteria for filtering payments
     * @return a Specification for the Payment entity
     */
    public static Specification<Payment> bySearchCriteria(PaymentSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getDescription() != null && !searchCriteria.getDescription().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + searchCriteria.getDescription() + "%"));
            }
            if (searchCriteria.getDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("date"), searchCriteria.getDate()));
            }
            if (searchCriteria.getAmount() != null) {
                predicates.add(criteriaBuilder.equal(root.get("amount"), searchCriteria.getAmount()));
            }
            if (searchCriteria.getSubject() != null && !searchCriteria.getSubject().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("subject"), "%" + searchCriteria.getSubject() + "%"));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                Join<Payment, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(customerJoin.get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getYearName() != null) {
                Join<Payment, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("year"), searchCriteria.getYearName()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
