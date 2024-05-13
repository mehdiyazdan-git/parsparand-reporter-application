package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceiptItem;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseReceiptItemMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "productId", target = "product.id")
    WarehouseReceiptItem toEntity(WarehouseReceiptItemDto warehouseReceiptItemDto);

    @InheritInverseConfiguration(name = "toEntity")
    WarehouseReceiptItemDto toDto(WarehouseReceiptItem warehouseReceiptItem);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    WarehouseReceiptItem partialUpdate(WarehouseReceiptItemDto warehouseReceiptItemDto, @MappingTarget WarehouseReceiptItem warehouseReceiptItem);
}