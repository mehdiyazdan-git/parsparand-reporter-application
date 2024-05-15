package com.armaninvestment.parsparandreporterapplication.specifications;

import com.armaninvestment.parsparandreporterapplication.entities.Product;
import com.armaninvestment.parsparandreporterapplication.searchForms.ProductSearch;
import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    /**
     * Creates a specification based on the provided ProductSearch criteria.
     * @param searchCriteria the criteria for filtering products
     * @return a Specification for the Product entity
     */
    public static Specification<Product> bySearchCriteria(ProductSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getProductCode() != null && !searchCriteria.getProductCode().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("productCode"), "%" + searchCriteria.getProductCode() + "%"));
            }
            if (searchCriteria.getProductName() != null && !searchCriteria.getProductName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("productName"), "%" + searchCriteria.getProductName() + "%"));
            }
            if (searchCriteria.getMeasurementIndex() != null && !searchCriteria.getMeasurementIndex().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("measurementIndex"), searchCriteria.getMeasurementIndex()));
            }
            if (searchCriteria.getProductTypeCaption() != null && !searchCriteria.getProductTypeCaption().isEmpty()) {
                for (ProductType type : ProductType.values()) {
                    if (type.getCaption().equals(searchCriteria.getProductTypeCaption())) {
                        predicates.add(criteriaBuilder.equal(root.get("productType"), type.getValue()));
                        break;
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> getSelectSpecification(String searchParam) {
         return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("productName"), "%" + searchParam + "%");
    }
}
