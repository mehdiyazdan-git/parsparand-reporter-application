package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.AdjustmentSearch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AdjustmentSpecification {

    /**
     * Creates a specification based on the provided AdjustmentSearch criteria.
     * @param searchCriteria the criteria for filtering adjustments
     * @return a Specification for the Adjustment entity
     */
    public static Specification<Adjustment> bySearchCriteria(AdjustmentSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getAdjustmentNumber() != null) {
                predicates.add(criteriaBuilder.equal(root.get("adjustmentNumber"), searchCriteria.getAdjustmentNumber()));
            }
            if (searchCriteria.getAdjustmentDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("adjustmentDate"), searchCriteria.getAdjustmentDate()));
            }
            if (searchCriteria.getDescription() != null && !searchCriteria.getDescription().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + searchCriteria.getDescription() + "%"));
            }
            if (searchCriteria.getUnitPrice() != null) {
                predicates.add(criteriaBuilder.equal(root.get("unitPrice"), searchCriteria.getUnitPrice()));
            }
            if (searchCriteria.getQuantity() != null) {
                predicates.add(criteriaBuilder.equal(root.get("quantity"), searchCriteria.getQuantity()));
            }
            if (searchCriteria.getAdjustmentType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("adjustmentType"), searchCriteria.getAdjustmentType()));
            }
            if (searchCriteria.getInvoiceNumber() != null) {
                Join<Adjustment, Invoice> invoiceJoin = root.join("invoice", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(invoiceJoin.get("invoiceNumber"), searchCriteria.getInvoiceNumber()));
            }
            if (searchCriteria.getJalaliYear() != null) {
                Join<Adjustment, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("name"), searchCriteria.getJalaliYear()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
