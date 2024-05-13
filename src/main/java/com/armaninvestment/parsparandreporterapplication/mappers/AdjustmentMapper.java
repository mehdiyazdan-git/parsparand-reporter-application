package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Adjustment;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdjustmentMapper {
    @Mapping(source = "invoiceId", target = "invoice.id")
    Adjustment toEntity(AdjustmentDto adjustmentDto);

    @Mapping(source = "invoice.id", target = "invoiceId")
    AdjustmentDto toDto(Adjustment adjustment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "invoiceId", target = "invoice.id")
    Adjustment partialUpdate(AdjustmentDto adjustmentDto, @MappingTarget Adjustment adjustment);
}