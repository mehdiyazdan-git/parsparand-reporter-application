package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.searchForms.WarehouseReceiptSearch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WarehouseReceiptSpecification {

    /**
     * Creates a specification based on the provided WarehouseReceiptSearch criteria.
     * @param searchCriteria the criteria for filtering warehouse receipts
     * @return a Specification for the WarehouseReceipt entity
     */
    public static Specification<WarehouseReceipt> bySearchCriteria(WarehouseReceiptSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getWarehouseReceiptNumber() != null) {
                predicates.add(criteriaBuilder.equal(root.get("warehouseReceiptNumber"), searchCriteria.getWarehouseReceiptNumber()));
            }
            if (searchCriteria.getWarehouseReceiptDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("warehouseReceiptDate"), searchCriteria.getWarehouseReceiptDate()));
            }
            if (searchCriteria.getWarehouseReceiptDescription() != null && !searchCriteria.getWarehouseReceiptDescription().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("warehouseReceiptDescription"), "%" + searchCriteria.getWarehouseReceiptDescription() + "%"));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                Join<WarehouseReceipt, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(customerJoin.get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getJalaliYear() != null) {
                Join<WarehouseReceipt, Year> yearJoin = root.join("year", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(yearJoin.get("name"), searchCriteria.getJalaliYear()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
