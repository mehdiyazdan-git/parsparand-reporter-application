package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.searchForms.WarehouseReceiptSearch;
import jakarta.persistence.criteria.*;
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
            if (searchCriteria.getNotInvoiced() != null && searchCriteria.getNotInvoiced()) {
                Join<WarehouseReceipt, WarehouseInvoice> invoiceJoin = root.join("warehouseInvoices", JoinType.LEFT);
                predicates.add(criteriaBuilder.isNull(invoiceJoin.get("invoice")));
            }
            if (searchCriteria.getCustomerId() != null){
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), searchCriteria.getCustomerId()));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("customer").get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getJalaliYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("jalaliYear"), searchCriteria.getJalaliYear()));
            }
            // If the search criteria contains a total quantity, create a subquery to find the sum of the quantities of the report items
            if (searchCriteria.getTotalQuantity() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                // Start from the ReportItem class
                Root<ReportItem> subRoot = subquery.from(ReportItem.class);
                // Select the sum of the quantities
                subquery.select(criteriaBuilder.sum(subRoot.get("quantity")));
                // Only include report items with the same warehouse receipt as the root
                subquery.where(criteriaBuilder.equal(subRoot.get("warehouseReceipt"), root));
                // Add a predicate to the list of predicates, checking if the sum of the quantities is less than or equal to the search criteria total quantity
                predicates.add(criteriaBuilder.le(subquery, searchCriteria.getTotalQuantity()));
            }
            if (searchCriteria.getTotalPrice() != null) {
                Subquery<Double> subquery = query.subquery(Double.class);
                Root<ReportItem> subRoot = subquery.from(ReportItem.class);
                subquery.select(criteriaBuilder.sum(criteriaBuilder.prod(subRoot.get("unitPrice"), subRoot.get("quantity"))));
                subquery.where(criteriaBuilder.equal(subRoot.get("warehouseReceipt"), root));

                predicates.add(criteriaBuilder.le(subquery, searchCriteria.getTotalPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
