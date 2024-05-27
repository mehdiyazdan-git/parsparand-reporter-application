package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecification {

    /**
     * Creates a specification based on the provided InvoiceSearch criteria.
     * @param searchCriteria the criteria for filtering invoices
     * @return a Specification for the Invoice entity
     */
    public static Specification<Invoice> bySearchCriteria(InvoiceSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getInvoiceNumber() != null) {
                predicates.add(criteriaBuilder.equal(root.get("invoiceNumber"), searchCriteria.getInvoiceNumber()));
            }
            if (searchCriteria.getIssuedDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("issuedDate"), searchCriteria.getIssuedDate()));
            }
            if (searchCriteria.getSalesType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("salesType"), searchCriteria.getSalesType()));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                Join<Invoice, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(customerJoin.get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getInvoiceStatusName() != null && !searchCriteria.getInvoiceStatusName().isEmpty()) {
                Join<Invoice, InvoiceStatus> statusJoin = root.join("invoiceStatus", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(statusJoin.get("statusName"), "%" + searchCriteria.getInvoiceStatusName() + "%"));
            }
            if (searchCriteria.getJalaliYear() != null) {
                Join<Invoice, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("name"), searchCriteria.getJalaliYear()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
