package com.armaninvestment.parsparandreporterapplication.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentSubjectConverter implements AttributeConverter<PaymentSubject, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PaymentSubject paymentSubject) {
        if (paymentSubject == null) {
            return null;
        }
        return switch (paymentSubject.name()) {
            case "PRODUCT" -> 1;
            case "INSURANCEDEPOSIT" -> 2;
            case "PERFORMANCEBOUND" -> 3;
            case "ADVANCEDPAYMENT" -> 4;
            default -> null;
        };
    }

    @Override
    public PaymentSubject convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return switch (dbData) {
            case 1 -> PaymentSubject.PRODUCT;
            case 2 -> PaymentSubject.INSURANCEDEPOSIT;
            case 3 -> PaymentSubject.PERFORMANCEBOUND;
            case 4 -> PaymentSubject.ADVANCEDPAYMENT;
            default -> null;
        };
    }
}
