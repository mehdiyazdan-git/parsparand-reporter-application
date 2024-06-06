package com.armaninvestment.parsparandreporterapplication.config;

import com.armaninvestment.parsparandreporterapplication.enums.PaymentSubject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPaymentSubjectConverter implements Converter<String, PaymentSubject> {

    @Override
    public PaymentSubject convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return PaymentSubject.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // or throw an exception if invalid value should not be accepted
        }
    }
}
