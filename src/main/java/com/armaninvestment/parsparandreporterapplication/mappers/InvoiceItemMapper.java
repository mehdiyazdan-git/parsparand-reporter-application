package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.InvoiceItem;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceItemMapper {
    @Mapping(source = "warehouseReceiptId", target = "warehouseReceipt.id")
    @Mapping(source = "productId", target = "product.id")
    InvoiceItem toEntity(InvoiceItemDto invoiceItemDto);

    @InheritInverseConfiguration(name = "toEntity")
    InvoiceItemDto toDto(InvoiceItem invoiceItem);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceItem partialUpdate(InvoiceItemDto invoiceItemDto, @MappingTarget InvoiceItem invoiceItem);
}