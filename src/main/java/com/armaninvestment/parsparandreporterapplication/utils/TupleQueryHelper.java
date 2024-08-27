package com.armaninvestment.parsparandreporterapplication.utils;

import jakarta.persistence.Tuple;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TupleQueryHelper<D, T> {

    private final Class<D> dtoClass;

    Logger logger = org.apache.logging.log4j.LogManager.getLogger(TupleQueryHelper.class);

    public TupleQueryHelper(Class<D> dtoClass) {
        this.dtoClass = dtoClass;
    }

    public List<D> convertToDtoList(List<T> tuples) {
        return tuples.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private D convertToDto(T tuple) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            populateDtoFields(dto, tuple);
            return dto;
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new DTOConversionException(String.format("Error converting tuple to DTO of type %s", dtoClass.getName()), e, e);
        }
    }

    private void populateDtoFields(Object dto, Object tuple) {
        for (Field field : dto.getClass().getDeclaredFields()) {
            try {
                // Skip nested classes and array fields
                if (isComposite(field.getType()) || field.getType().isArray()) {
                    continue;
                }
                setValue(dto, tuple, field);
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                String errorMessage = String.format("Error setting field '%s' on DTO of type %s",
                        field.getName(), dto.getClass().getName());
                logger.error(errorMessage, e);
            }
        }
    }

    private void setValue(Object dto, Object tuple, Field field) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object fieldValue = ((Tuple) tuple).get(field.getName(), field.getType());
        field.setAccessible(true);
        field.set(dto, fieldValue);
    }

    private static boolean isComposite(Class<?> clazz) {
        return !clazz.isPrimitive() && !clazz.equals(String.class) && !Number.class.isAssignableFrom(clazz) &&
                !clazz.equals(Boolean.class) && !clazz.equals(Character.class) && !clazz.equals(LocalDate.class) &&
                !clazz.equals(LocalDateTime.class) && !clazz.equals(Set.class);
    }

    public static class DTOConversionException extends RuntimeException {
        public DTOConversionException(String message, Throwable cause, Exception e) {
            super((message != null && !message.isEmpty()) ? message : e.getMessage(), cause);
        }
    }
}