package com.armaninvestment.parsparandreporterapplication.specifications;

import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.User;
import com.armaninvestment.parsparandreporterapplication.searchForms.UserSearch;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    /**
     * Creates a specification based on the provided UserSearch criteria.
     * @param searchCriteria the criteria for filtering users
     * @return a Specification for the User entity
     */
    public static Specification<User> bySearchCriteria(UserSearch searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.getId()));
            }
            if (searchCriteria.getEmail() != null && !searchCriteria.getEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + searchCriteria.getEmail().toLowerCase() + "%"));
            }
            if (searchCriteria.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), searchCriteria.getEnabled()));
            }
            if (searchCriteria.getFirstname() != null && !searchCriteria.getFirstname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")), "%" + searchCriteria.getFirstname().toLowerCase() + "%"));
            }
            if (searchCriteria.getLastname() != null && !searchCriteria.getLastname().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastname")), "%" + searchCriteria.getLastname().toLowerCase() + "%"));
            }
            if (searchCriteria.getPassword() != null && !searchCriteria.getPassword().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("password")), "%" + searchCriteria.getPassword().toLowerCase() + "%"));
            }
            if (searchCriteria.getRole() != null && !searchCriteria.getRole().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), "%" + searchCriteria.getRole().toLowerCase() + "%"));
            }
            if (searchCriteria.getUsername() != null && !searchCriteria.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + searchCriteria.getUsername().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<User> getSelectSpecification(String searchParam){
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (searchParam !=null && !searchParam.isEmpty()) {
                predicate =(cb.like(root.get("username"), "%" + searchParam.trim() + "%"));
            }
            return predicate;
        };

    }
}
