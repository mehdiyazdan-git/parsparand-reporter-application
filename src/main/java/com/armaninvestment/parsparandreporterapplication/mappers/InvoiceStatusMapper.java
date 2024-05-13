package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporterapplication.entities.InvoiceStatus;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceStatusMapper {
    InvoiceStatus toEntity(InvoiceStatusDto invoiceStatusDto);

    InvoiceStatusDto toDto(InvoiceStatus invoiceStatus);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceStatus partialUpdate(InvoiceStatusDto invoiceStatusDto, @MappingTarget InvoiceStatus invoiceStatus);
}