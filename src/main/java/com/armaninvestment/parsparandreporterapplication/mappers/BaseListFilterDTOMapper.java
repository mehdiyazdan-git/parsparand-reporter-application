package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.BaseFilter;
import com.armaninvestment.parsparandreporterapplication.dtos.BaseListFilterDTO;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BaseListFilterDTOMapper {
    @Mapping(source = "sortingSortBy", target = "sorting.sortBy")
    @Mapping(source = "sortingOrder", target = "sorting.order")
    @Mapping(source = "paginationSize", target = "pagination.size")
    @Mapping(source = "paginationPage", target = "pagination.page")
    BaseListFilterDTO toEntity(BaseFilter baseFilter);

    @InheritInverseConfiguration(name = "toEntity")
    BaseFilter toDto(BaseListFilterDTO baseListFilterDTO);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BaseListFilterDTO partialUpdate(BaseFilter baseFilter, @MappingTarget BaseListFilterDTO baseListFilterDTO);
}