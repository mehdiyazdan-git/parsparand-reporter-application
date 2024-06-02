package com.armaninvestment.parsparandreporterapplication.specifications;

import com.armaninvestment.parsparandreporterapplication.entities.Report;
import com.armaninvestment.parsparandreporterapplication.entities.ReportItem;
import com.armaninvestment.parsparandreporterapplication.searchForms.ReportSearch;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ReportSpecification implements Specification<Report> {
    private final ReportSearch reportSearch;

    @Override
    public Predicate toPredicate(Root<Report> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
        assert reportSearch != null;
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
            predicates.add(cb.equal(root.get("jalaliYear"), reportSearch.getJalaliYear()));
        }
        if (reportSearch.getTotalQuantity() != null) {
            Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
            Root<ReportItem> subRoot = subquery.from(ReportItem.class);
            subquery.select(cb.sum(subRoot.get("quantity")));
            subquery.where(cb.equal(subRoot.get("report"), root));

            predicates.add(cb.le(subquery, reportSearch.getTotalQuantity()));
        }
        if (reportSearch.getTotalPrice() != null) {
            Subquery<Double> subquery = criteriaQuery.subquery(Double.class);
            Root<ReportItem> subRoot = subquery.from(ReportItem.class);
            subquery.select(cb.sum(cb.prod(subRoot.get("unitPrice"), subRoot.get("quantity"))));
            subquery.where(cb.equal(subRoot.get("report"), root));

            predicates.add(cb.le(subquery, reportSearch.getTotalPrice()));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }

}
