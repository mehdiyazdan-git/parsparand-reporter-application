package com.armaninvestment.parsparandreporterapplication.specifications;

import com.armaninvestment.parsparandreporterapplication.entities.VATRate;

import com.armaninvestment.parsparandreporterapplication.searchForms.VATRateSearch;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class VATRateSpecification {

    /**
     * Creates a specification based on the provided VATRateSearch criteria.
     * @param searchCriteria the criteria for filtering VAT rates
     * @return a Specification for the VATRate entity
     */
    public static Specification<VATRate> bySearchCriteria(VATRateSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            List<Expression<Boolean>> expressions = predicate.getExpressions();

            if (searchCriteria.getRate() != null) {
                expressions.add(criteriaBuilder.equal(root.get("rate"), searchCriteria.getRate()));
            }
            if (searchCriteria.getEffectiveFromStart() != null) {
                expressions.add(criteriaBuilder.greaterThanOrEqualTo(root.get("effectiveFrom"), searchCriteria.getEffectiveFromStart()));
            }
            if (searchCriteria.getEffectiveFromEnd() != null) {
                expressions.add(criteriaBuilder.lessThanOrEqualTo(root.get("effectiveFrom"), searchCriteria.getEffectiveFromEnd()));
            }

            return predicate;
        };
    }

    /**
     * Creates a specification for searching VAT rates by a general search parameter (e.g., rate or effective date).
     * @param searchParam the general search parameter
     * @return a Specification for the VATRate entity
     */
    public static Specification<VATRate> getSelectSpecification(String searchParam) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (searchParam != null && !searchParam.isEmpty()) {
                try {
                    // Try to search by rate if searchParam is a number
                    Float rate = Float.parseFloat(searchParam);
                    predicate = cb.equal(root.get("rate"), rate);
                } catch (NumberFormatException e) {
                    // If it's not a number, try to search by effectiveFrom date
                    LocalDate effectiveDate;
                    try {
                        effectiveDate = LocalDate.parse(searchParam);
                        predicate = cb.equal(root.get("effectiveFrom"), effectiveDate);
                    } catch (Exception ex) {
                        // If searchParam is neither a number nor a valid date, leave predicate unchanged
                    }
                }
            }
            return predicate;
        };
    }
}
