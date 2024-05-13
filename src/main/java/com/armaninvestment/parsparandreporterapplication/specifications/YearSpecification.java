package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.searchForms.YearSearch;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class YearSpecification {

    /**
     * Creates a specification based on the provided YearSearch criteria.
     * @param searchCriteria the criteria for filtering years
     * @return a Specification for the Year entity
     */
    public static Specification<Year> bySearchCriteria(YearSearch searchCriteria) {
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
