package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {WarehouseReceiptItemMapper.class})
public interface WarehouseReceiptMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "customerId", target = "customer.id")
    WarehouseReceipt toEntity(WarehouseReceiptDto warehouseReceiptDto);

    @AfterMapping
    default void linkWarehouseReceiptItems(@MappingTarget WarehouseReceipt warehouseReceipt) {
        warehouseReceipt.getWarehouseReceiptItems().forEach(warehouseReceiptItem -> warehouseReceiptItem.setWarehouseReceipt(warehouseReceipt));
    }

    @InheritInverseConfiguration(name = "toEntity")
    WarehouseReceiptDto toDto(WarehouseReceipt warehouseReceipt);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    WarehouseReceipt partialUpdate(WarehouseReceiptDto warehouseReceiptDto, @MappingTarget WarehouseReceipt warehouseReceipt);
}