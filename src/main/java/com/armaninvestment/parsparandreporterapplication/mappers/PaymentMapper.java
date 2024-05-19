package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.PaymentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Payment;
import org.mapstruct.*;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;

import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(paymentDto.getPaymentDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(paymentDto.getPaymentDate()))")
    Payment toEntity(PaymentDto paymentDto);

    @Mapping(source = "year.id", target = "yearId")
    @Mapping(source = "year.name", target = "yearName")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    PaymentDto toDto(Payment payment);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(paymentDto.getPaymentDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(paymentDto.getPaymentDate()))")
    Payment partialUpdate(PaymentDto paymentDto, @MappingTarget Payment payment);

    default Integer extractJalaliYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return jalaliDate.getYear();
    }

    default Integer extractMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return jalaliDate.getMonthPersian().getValue();
    }
}
