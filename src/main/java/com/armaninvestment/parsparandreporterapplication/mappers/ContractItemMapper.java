package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ContractItemDto;
import com.armaninvestment.parsparandreporterapplication.entities.ContractItem;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractItemMapper {
    @Mapping(source = "productId", target = "product.id")
    ContractItem toEntity(ContractItemDto contractItemDto);

    @Mapping(source = "product.id", target = "productId")
    ContractItemDto toDto(ContractItem contractItem);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "productId", target = "product.id")
    ContractItem partialUpdate(ContractItemDto contractItemDto, @MappingTarget ContractItem contractItem);
}