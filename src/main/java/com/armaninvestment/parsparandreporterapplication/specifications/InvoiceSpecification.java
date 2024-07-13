package com.armaninvestment.parsparandreporterapplication.specifications;


import com.armaninvestment.parsparandreporterapplication.entities.*;
import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.armaninvestment.parsparandreporterapplication.searchForms.InvoiceSearch;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecification {

    /**
     * Creates a specification based on the provided InvoiceSearch criteria.
     * @param searchCriteria the criteria for filtering invoices
     * @return a Specification for the Invoice entity
     */
    public static Specification<Invoice> bySearchCriteria(InvoiceSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getInvoiceNumber() != null) {
                predicates.add(criteriaBuilder.equal(root.get("invoiceNumber"), searchCriteria.getInvoiceNumber()));
            }
            if(searchCriteria.getContractId() != null) {
                Join<Invoice, Contract> contractJoin = root.join("contract", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(contractJoin.get("id"), searchCriteria.getContractId()));
            }
            if(searchCriteria.getCustomerName() != null) {
                Join<Invoice, Contract> contractJoin = root.join("contract", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(contractJoin.get("contractNumber"), searchCriteria.getContractNumber()));
            }
            if(searchCriteria.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), searchCriteria.getCustomerId()));
            }
            if (searchCriteria.getIssuedDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("issuedDate"), searchCriteria.getIssuedDate()));
            }
            if (searchCriteria.getSalesType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("salesType"), searchCriteria.getSalesType()));
            }
            if (searchCriteria.getCustomerName() != null && !searchCriteria.getCustomerName().isEmpty()) {
                Join<Invoice, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(customerJoin.get("name"), "%" + searchCriteria.getCustomerName() + "%"));
            }
            if (searchCriteria.getInvoiceStatusName() != null && !searchCriteria.getInvoiceStatusName().isEmpty()) {
                Join<Invoice, InvoiceStatus> statusJoin = root.join("invoiceStatus", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(statusJoin.get("statusName"), "%" + searchCriteria.getInvoiceStatusName() + "%"));
            }
            if (searchCriteria.getJalaliYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("jalaliYear"), searchCriteria.getJalaliYear()));
            }
            if (searchCriteria.getTotalQuantity() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<InvoiceItem> subRoot = subquery.from(InvoiceItem.class);
                subquery.select(criteriaBuilder.sum(subRoot.get("quantity")));
                subquery.where(criteriaBuilder.equal(subRoot.get("invoice"), root));

                predicates.add(criteriaBuilder.le(subquery, searchCriteria.getTotalQuantity()));
            }
            if (searchCriteria.getTotalPrice() != null) {
                Subquery<Double> subquery = query.subquery(Double.class);
                Root<InvoiceItem> subRoot = subquery.from(InvoiceItem.class);
                subquery.select(criteriaBuilder.sum(criteriaBuilder.prod(subRoot.get("unitPrice"), subRoot.get("quantity"))));
                subquery.where(criteriaBuilder.equal(subRoot.get("invoice"), root));

                predicates.add(criteriaBuilder.le(subquery, searchCriteria.getTotalPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
