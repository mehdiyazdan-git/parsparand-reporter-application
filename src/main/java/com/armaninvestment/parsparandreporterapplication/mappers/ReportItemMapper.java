package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReportItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.ReportItem;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportItemMapper {
    @Mapping(source = "warehouseReceiptId", target = "warehouseReceipt.id")
    @Mapping(source = "customerId", target = "customer.id")
    ReportItem toEntity(ReportItemDto reportItemDto);

    @InheritInverseConfiguration(name = "toEntity")
    ReportItemDto toDto(ReportItem reportItem);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ReportItem partialUpdate(ReportItemDto reportItemDto, @MappingTarget ReportItem reportItem);
}