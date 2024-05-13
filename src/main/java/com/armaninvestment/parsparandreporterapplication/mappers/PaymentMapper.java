package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.PaymentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Payment;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "customerId", target = "customer.id")
    Payment toEntity(PaymentDto paymentDto);

    @InheritInverseConfiguration(name = "toEntity")
    PaymentDto toDto(Payment payment);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Payment partialUpdate(PaymentDto paymentDto, @MappingTarget Payment payment);
}