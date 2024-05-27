package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.InvoiceItem;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceItemMapper {
    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "warehouseReceiptId", target = "warehouseReceipt.id")
    InvoiceItem toEntity(InvoiceItemDto invoiceItemDto);

    @InheritInverseConfiguration(name = "toEntity")
    InvoiceItemDto toDto(InvoiceItem invoiceItem);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceItem partialUpdate(InvoiceItemDto invoiceItemDto, @MappingTarget InvoiceItem invoiceItem);
}