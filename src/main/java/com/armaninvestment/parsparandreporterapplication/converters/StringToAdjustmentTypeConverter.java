package com.armaninvestment.parsparandreporterapplication.converters;

import com.armaninvestment.parsparandreporterapplication.enums.AdjustmentType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToAdjustmentTypeConverter implements Converter<String, AdjustmentType> {

    @Override
    public AdjustmentType convert(String source) {
        if (source == null) {
            return null;
        }
        try {
            int value = Integer.parseInt(source);
            return AdjustmentType.fromValue(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid AdjustmentType value: " + source);
        }
    }
}
