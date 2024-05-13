package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.Report;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReportSpecification {

    /**
     * Creates a specification based on the provided ReportSearch criteria.
     * @param searchCriteria the criteria for filtering reports
     * @return a Specification for the Report entity
     */
    public static Specification<Report> bySearchCriteria(ReportSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getExplanation() != null && !searchCriteria.getExplanation().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("explanation"), "%" + searchCriteria.getExplanation() + "%"));
            }
            if (searchCriteria.getDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("date"), searchCriteria.getDate()));
            }
            if (searchCriteria.getYearName() != null) {
                // Assuming the Report entity has a relationship with a Year entity
                Join<Report, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("year"), searchCriteria.getYearName()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
