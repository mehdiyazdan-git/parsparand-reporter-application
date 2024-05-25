package com.armaninvestment.parsparandreporterapplication.specifications;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import com.github.eloyzone.jalalicalendar.DateConverter;


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
            if (searchCriteria.getPaymentDescryption() != null && !searchCriteria.getPaymentDescryption().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("paymentDescryption"), "%" + searchCriteria.getPaymentDescryption() + "%"));
            }
            if (searchCriteria.getPaymentDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentDate"), searchCriteria.getPaymentDate()));
            }
            if (searchCriteria.getPaymentAmount() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentAmount"), searchCriteria.getPaymentAmount()));
            }
            if (searchCriteria.getPaymentSubject() != null && !searchCriteria.getPaymentSubject().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("paymentSubject"), "%" + searchCriteria.getPaymentSubject() + "%"));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                Join<Payment, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(customerJoin.get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getYearName() != null) {
                Join<Payment, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("name"), searchCriteria.getYearName()));
            }
            if (searchCriteria.getJalaliYear() != null) {
                Join<Payment, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("name"), searchCriteria.getJalaliYear()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
