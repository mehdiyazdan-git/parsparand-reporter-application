package com.armaninvestment.parsparandreporterapplication.utils;


import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import org.hibernate.mapping.AggregateColumn;

import java.util.List;

import java.util.stream.Collectors;

public class QueryHelper<Root> {

    public  List<Expression<?>> groupBy(CriteriaQuery<Tuple> multiSelect) {
        // if multiselect has any AggregateColumn, we return the list of expressions which are not AggregateColumn
        // otherwise return empty list
        return multiSelect.getSelection().getCompoundSelectionItems()
                .stream()
                .filter(selectionItem -> !(selectionItem instanceof AggregateColumn))
                .map(selectionItem -> (Expression<?>) selectionItem)
                .collect(Collectors.toList());
    }
    // implement set orderBy method

    public  Order orderBy(CriteriaQuery<Tuple> multiSelect, String sortBy, String sortDir) {
        // if sortBy is indirect member of root so probably it is a join
        // so the derivative join member should be used to order
        return (Order) multiSelect.getSelection().getCompoundSelectionItems()
                .stream()
                .filter( selection -> selection.getAlias().equals(sortBy))
                .map(selection -> multiSelect.getRestriction().getAlias().equals(sortBy)
                        ? multiSelect.getRestriction().getAlias()
                        : selection.getAlias()
                )
                .collect(Collectors.toList());

    }
}

// // Sorting
//        switch (Objects.requireNonNull(sortBy)) {
//            case "totalPrice" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalPrice) : cb.desc(totalPrice));
//            case "totalQuantity" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(totalQuantity) : cb.desc(totalQuantity));
//            case "customerName" -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(root.get("customer").get("name")) : cb.desc(root.get("customer").get("name")));
//            default -> cq.orderBy(sortDir.equalsIgnoreCase("asc") ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy)));
//        }