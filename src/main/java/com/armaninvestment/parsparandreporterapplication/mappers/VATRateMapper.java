package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.VATRateDto;
import com.armaninvestment.parsparandreporterapplication.entities.VATRate;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface VATRateMapper {
    VATRate toEntity(VATRateDto VATRateDto);

    VATRateDto toDto(VATRate VATRate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    VATRate partialUpdate(VATRateDto VATRateDto, @MappingTarget VATRate VATRate);
}