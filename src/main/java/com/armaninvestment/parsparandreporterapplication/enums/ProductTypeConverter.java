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
        return switch (dbData) {
            case 2 -> ProductType.MAIN;
            case 6 -> ProductType.SCRAPT;
            case 1 -> ProductType.RAWMATERIAL;
            default -> throw new IllegalArgumentException("Unknown value: " + dbData);
        };
    }
}
