package com.armaninvestment.parsparandreporterapplication.specifications;

import com.armaninvestment.parsparandreporterapplication.entities.Report;
import com.armaninvestment.parsparandreporterapplication.entities.ReportItem;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ReportSpecification implements Specification<Report> {

    private final ReportSearch reportSearch;

    @Override
    public Predicate toPredicate(Root<Report> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (reportSearch.getId() != null) {
            predicates.add(cb.equal(root.get("id"), reportSearch.getId()));
        }
        if (reportSearch.getReportExplanation() != null && !reportSearch.getReportExplanation().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("reportExplanation")), "%" + reportSearch.getReportExplanation().toLowerCase() + "%"));
        }
        if (reportSearch.getReportDate() != null) {
            predicates.add(cb.equal(root.get("reportDate"), reportSearch.getReportDate()));
        }
        if (reportSearch.getJalaliYear() != null) {
            Join<Report, Year> yearJoin = root.join("year", JoinType.LEFT);
            predicates.add(cb.equal(yearJoin.get("name"), reportSearch.getJalaliYear()));
        }

        if (currentQueryIsCountRecords(criteriaQuery)) {
            root.join("reportItems", JoinType.LEFT);
        } else {
            root.fetch("reportItems", JoinType.LEFT);
        }

        return criteriaQuery
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .distinct(true)
                .getRestriction();
    }

    private boolean currentQueryIsCountRecords(CriteriaQuery<?> criteriaQuery) {
        return criteriaQuery.getResultType() == Long.class || criteriaQuery.getResultType() == long.class;
    }
}
