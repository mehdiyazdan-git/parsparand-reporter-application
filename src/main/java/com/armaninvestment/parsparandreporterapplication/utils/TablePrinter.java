package com.armaninvestment.parsparandreporterapplication.utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * A utility class for printing data in a tabular format.
 **/
public class TablePrinter {
    /**
     * Prints the data in a tabular format.
     * @param data the data to be printed
     * @param clazz the class of the data
     * @param <T> the type of the data
     */
    public static <T> void printInTableFormat(List<T> data, Class<T> clazz) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data to display");
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        List<Field> filteredFields = Stream.of(fields)
                .filter(field -> !field.getType().isArray() && !Collection.class.isAssignableFrom(field.getType()))
                .toList();

        String[] headers = new String[filteredFields.size()];
        for (int i = 0; i < filteredFields.size(); i++) {
            headers[i] = filteredFields.get(i).getName();
        }

        String format = createFormatString(filteredFields);

        // Print headers
        System.out.printf(format, (Object[]) headers);
        System.out.println("-".repeat(format.length() - 2 * headers.length));

        // Print each data row
        for (T item : data) {
            Object[] values = new Object[filteredFields.size()];
            for (int i = 0; i < filteredFields.size(); i++) {
                filteredFields.get(i).setAccessible(true);
                try {
                    Object value = filteredFields.get(i).get(item);
                    values[i] = formatValue(value);
                } catch (IllegalAccessException e) {
                    values[i] = "N/A";
                }
            }
            System.out.printf(format, values);
        }
    }

    private static String createFormatString(List<Field> fields) {
        StringBuilder format = new StringBuilder();
        for (Field field : fields) {
            format.append("%-").append(20).append("s ");
        }
        format.append("%n");
        return format.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else if (value instanceof Date) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format((Date) value);
        } else {
            return value.toString();
        }
    }
}


