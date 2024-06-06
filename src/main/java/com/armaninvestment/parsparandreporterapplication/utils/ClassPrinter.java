package com.armaninvestment.parsparandreporterapplication.utils;

import java.lang.reflect.Field;

public class ClassPrinter {
    /**
     * Prints the details of a class instance in the format fieldName(value,type).
     *
     * @param clazz The class to print information about.
     * @param obj An instance of the class.
     * @return A formatted string containing the class instance details.
     * @throws IllegalAccessException If accessing private fields fails.
     */
    public static String printClass(Class<?> clazz, Object obj) throws IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getSimpleName()).append(" {");

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // Allow access to private fields
            sb.append("\n  ").append(field.getName()).append("(")
                    .append(field.get(obj) == null ? "null" : field.get(obj))
                    .append(",").append(field.getType().getSimpleName()).append(")");
        }

        sb.append("\n}");
        return sb.toString();
    }
}

