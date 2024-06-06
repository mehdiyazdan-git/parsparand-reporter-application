package com.armaninvestment.parsparandreporterapplication.enums;

public enum AdjustmentType {
    POSITIVE,
    NEGATIVE;

    public static AdjustmentType fromValue(int value) {
        return switch (value) {
            case 1 -> POSITIVE;
            case -1 -> NEGATIVE;
            default -> throw new IllegalArgumentException("Invalid AdjustmentType value: " + value);
        };
    }
    public static AdjustmentType fromValue(String value) {
        return switch (value) {
            case "POSITIVE" -> POSITIVE;
            case "NEGATIVE" -> NEGATIVE;
            default -> throw new IllegalArgumentException("Invalid AdjustmentType value: " + value);
        };
}}
