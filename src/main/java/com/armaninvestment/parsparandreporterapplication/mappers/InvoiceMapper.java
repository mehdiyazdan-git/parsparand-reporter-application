package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.entities.Invoice;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {InvoiceItemMapper.class})
public interface InvoiceMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "invoiceStatusId", target = "invoiceStatus.id")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "contractId", target = "contract.id")
    Invoice toEntity(InvoiceDto invoiceDto);

    @AfterMapping
    default void linkInvoiceItems(@MappingTarget Invoice invoice) {
        invoice.getInvoiceItems().forEach(invoiceItem -> invoiceItem.setInvoice(invoice));
    }

    @InheritInverseConfiguration(name = "toEntity")
    InvoiceDto toDto(Invoice invoice);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Invoice partialUpdate(InvoiceDto invoiceDto, @MappingTarget Invoice invoice);
}