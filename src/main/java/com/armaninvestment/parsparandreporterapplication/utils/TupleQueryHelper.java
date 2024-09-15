package com.armaninvestment.parsparandreporterapplication.utils;


import org.apache.logging.log4j.Logger;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hibernate.validator.internal.util.ReflectionHelper.isList;
import static org.springframework.util.StringUtils.capitalize;

public class TupleQueryHelper<D, T> {

    private final Class<D> dtoClass;

    Logger logger = org.apache.logging.log4j.LogManager.getLogger(TupleQueryHelper.class);

    public TupleQueryHelper(Class<D> dtoClass) {
        this.dtoClass = dtoClass;
    }
    // example dto :
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public class WarehouseReceiptDto implements Serializable {
//        private Long id;
//        private LocalDate warehouseReceiptDate;
//        private String warehouseReceiptDescription;
//        private Long warehouseReceiptNumber;
//        private Long customerId;
//        private String customerName;
//        private Long yearId;
//        private Long yearName;
//        private Long totalQuantity;
//        private Double totalPrice;
//        private List<WarehouseReceiptItemDto> warehouseReceiptItems = new ArrayList<>();
//    }
    //example Tuple: [134, 2024-04-12, حواله فروش 400 عدد بشکه pph2201 زرد - مشکی به شرکت نفت سپاهان, 14870, 219, شرکت نفت سپاهان, 4, 1403, 400, 3.6E9]
    public List<D> convertToDtoList(List<T> tuples) {
        return tuples.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    public D convertToDto(T tuple) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            Field[] fields = dtoClass.getDeclaredFields();

            // Determine the number of elements in the tuple
            int tupleSize = (int) tuple.getClass().getMethod("size").invoke(tuple);

            for (int i = 0; i < tupleSize && i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);

                if (isComposite(field.getType()) || isList(field.getType())) {
                    continue;
                }

                // Get the value from the tuple using its index
                Object tupleValue = tuple.getClass().getMethod("get", int.class).invoke(tuple, i);

                // Handle type conversions if necessary
                if (tupleValue != null && !field.getType().isInstance(tupleValue)) {
                    if (field.getType() == LocalDate.class && tupleValue instanceof java.sql.Date) {
                        tupleValue = ((java.sql.Date) tupleValue).toLocalDate();
                    } else if (field.getType() == LocalDateTime.class && tupleValue instanceof java.sql.Timestamp) {
                        tupleValue = ((java.sql.Timestamp) tupleValue).toLocalDateTime();
                    }
                    // Add more type conversions as needed
                }

                field.set(dto, tupleValue);
            }

            return dto;
        } catch (Exception e) {
            logger.error("Error converting tuple to DTO: " + e.getMessage(), e);
            return null;
        }
    }



    private static boolean isComposite(Class<?> clazz) {
        return !clazz.isPrimitive() && !clazz.equals(String.class) && !Number.class.isAssignableFrom(clazz) &&
                !clazz.equals(Boolean.class) && !clazz.equals(Character.class) && !clazz.equals(LocalDate.class) &&
                !clazz.equals(LocalDateTime.class) && !clazz.equals(Set.class);
    }
}