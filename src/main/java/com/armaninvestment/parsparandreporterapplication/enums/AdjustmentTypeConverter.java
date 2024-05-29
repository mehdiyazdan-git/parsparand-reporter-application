package com.armaninvestment.parsparandreporterapplication.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AdjustmentTypeConverter implements AttributeConverter<AdjustmentType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AdjustmentType adjustmentType) {
        if (adjustmentType == null) {
            return null;
        }
        return switch (adjustmentType.name()) {
            case "POSITIVE" -> 1;
            case "NEGATIVE" -> -1;
            default -> null;
        };
    }

    @Override
    public AdjustmentType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return switch (dbData) {
            case 1 -> AdjustmentType.POSITIVE;
            case -1 -> AdjustmentType.NEGATIVE;
            default -> null;
        };
    }
}
