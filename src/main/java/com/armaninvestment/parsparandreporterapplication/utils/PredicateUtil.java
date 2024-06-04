package com.armaninvestment.parsparandreporterapplication.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public class PredicateUtil {

    public enum SearchOperation {
        EQUALS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        LESS_THAN_OR_EQUAL_TO,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }

    public static Predicate getPredicate(Path<? extends Number> path, CriteriaBuilder criteriaBuilder,Comparable<? extends Number> value, SearchOperation operation) {
        switch (operation) {
            case EQUALS:
                return criteriaBuilder.equal(path, value);
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(path.as(Comparable.class), (Comparable) value);
            case LESS_THAN:
                return criteriaBuilder.lessThan(path.as(Comparable.class), (Comparable) value);
            case GREATER_THAN_OR_EQUAL_TO:
                return criteriaBuilder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case LESS_THAN_OR_EQUAL_TO:
                return criteriaBuilder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
            case CONTAINS:
                return criteriaBuilder.like(path.as(String.class), "%" + value + "%");
            case STARTS_WITH:
                return criteriaBuilder.like(path.as(String.class), value + "%");
            case ENDS_WITH:
                return criteriaBuilder.like(path.as(String.class), "%" + value);
            default:
                throw new IllegalArgumentException("Operation not supported");
        }
    }
}

