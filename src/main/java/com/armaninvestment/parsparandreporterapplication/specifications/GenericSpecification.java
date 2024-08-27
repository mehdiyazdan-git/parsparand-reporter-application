package com.armaninvestment.parsparandreporterapplication.specifications;

import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class GenericSpecification<S, E> implements Specification<E> {
    private final S searchCriteria;

    @Override
    public Predicate toPredicate(
            @Nonnull Root<E> root,
            @Nonnull CriteriaQuery<?> query,
            @Nonnull CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> predicates = new ArrayList<>();
        Field[] declaredFields = searchCriteria.getClass().getDeclaredFields();

        for (Field field : declaredFields) {
            try {
                field.setAccessible(true);
                Object value = field.get(searchCriteria);

                if (value != null) {
                    String fieldName = field.getName();
                    Predicate predicate = buildPredicate(criteriaBuilder, root, fieldName, value);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Root<E> root, String fieldName, Object value) {
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.");
            String joinEntityName = parts[0];
            String joinedFieldName = parts[1];
            return buildPredicateForJoinedField(criteriaBuilder, root, joinEntityName, joinedFieldName, value);
        } else {
            return buildPredicateForSimpleField(criteriaBuilder, root, fieldName, value);
        }
    }

    private Predicate buildPredicateForSimpleField(CriteriaBuilder criteriaBuilder, Root<E> root, String fieldName, Object value) {

        return switch ((value).getClass().getSimpleName()) {
            case "String" -> criteriaBuilder.like(root.get(fieldName), "%" + value + "%");
            case "Number", "Enum", "LocalDate", "Boolean" -> criteriaBuilder.equal(root.get(fieldName), value);
            default -> null;
        };
    }

    private Predicate buildPredicateForJoinedField(
            CriteriaBuilder criteriaBuilder,
            Root<E> root,
            String joinEntityName,
            String fieldName,
            Object value
    ) {
        EntityType<E> entityType = root.getModel();
        Set<Attribute<? super E, ?>> attributes = entityType.getAttributes();
        for (Attribute<? super E, ?> attribute : attributes) {
            if (attribute.getName().equals(fieldName)) {
                return criteriaBuilder.equal(root.join(joinEntityName).get(fieldName), value);
            }
        }
        return null;
    }

    // other methods and logic for the GenericSpecification class


}














