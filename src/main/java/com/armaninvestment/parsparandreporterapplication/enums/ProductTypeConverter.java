package com.armaninvestment.parsparandreporterapplication.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductTypeConverter implements AttributeConverter<ProductType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProductType productType) {
        if (productType == null) {
            return null;
        }
        return productType.getValue();
    }

    @Override
    public ProductType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return ProductType.fromValue(dbData);
    }

    public static ProductType fromString(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            int value = Integer.parseInt(dbData);
            return ProductType.fromValue(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ProductType value: " + dbData);
        }
    }

}
