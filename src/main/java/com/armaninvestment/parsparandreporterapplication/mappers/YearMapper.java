package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.YearDto;
import com.armaninvestment.parsparandreporterapplication.entities.Year;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface YearMapper {
    Year toEntity(YearDto yearDto);

    YearDto toDto(Year year);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Year partialUpdate(YearDto yearDto, @MappingTarget Year year);
}