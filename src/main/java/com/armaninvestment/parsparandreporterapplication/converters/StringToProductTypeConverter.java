package com.armaninvestment.parsparandreporterapplication.converters;

import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.armaninvestment.parsparandreporterapplication.enums.ProductTypeConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToProductTypeConverter implements Converter<String, ProductType> {

    @Override
    public ProductType convert(String source) {
        return ProductTypeConverter.fromString(source);
    }
}
