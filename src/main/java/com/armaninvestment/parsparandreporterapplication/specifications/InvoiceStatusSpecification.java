package com.armaninvestment.parsparandreporterapplication.specifications;

import com.armaninvestment.parsparandreporterapplication.entities.InvoiceStatus;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceStatusSearch;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class InvoiceStatusSpecification {

    /**
     * Creates a specification based on the provided InvoiceStatusSearch criteria.
     * @param searchCriteria the criteria for filtering invoice statuses
     * @return a Specification for the InvoiceStatus entity
     */
    public static Specification<InvoiceStatus> bySearchCriteria(InvoiceStatusSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            List<Expression<Boolean>> expressions = predicate.getExpressions();

            if (searchCriteria.getId() != null) {
                expressions.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getName() != null) {
                expressions.add(criteriaBuilder.equal(root.get("name"), searchCriteria.getName()));
            }

            return predicate;
        };
    }
}
