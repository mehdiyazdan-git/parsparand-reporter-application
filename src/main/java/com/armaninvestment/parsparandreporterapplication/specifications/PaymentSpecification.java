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
            if (searchCriteria.getPaymentDescription() != null && !searchCriteria.getPaymentDescription().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("paymentDescription"), "%" + searchCriteria.getPaymentDescription() + "%"));
            }
            if (searchCriteria.getPaymentDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentDate"), searchCriteria.getPaymentDate()));
            }
            if (searchCriteria.getPaymentAmount() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentAmount"), searchCriteria.getPaymentAmount()));
            }
            if (searchCriteria.getPaymentSubject() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentSubject"),searchCriteria.getPaymentSubject()));
            }
            if (searchCriteria.getCustomerId() != null ) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), searchCriteria.getCustomerId()));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("name"),"%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getYearName() != null) {
                Join<Payment, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("name"), searchCriteria.getYearName()));
            }
            if (searchCriteria.getJalaliYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("jalaliYear"), searchCriteria.getJalaliYear()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
