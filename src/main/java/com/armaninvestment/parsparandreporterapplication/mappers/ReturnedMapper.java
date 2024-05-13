package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReturnedMapper {
    @Mapping(source = "customerId", target = "customer.id")
    Returned toEntity(ReturnedDto returnedDto);

    @Mapping(source = "customer.id", target = "customerId")
    ReturnedDto toDto(Returned returned);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "customerId", target = "customer.id")
    Returned partialUpdate(ReturnedDto returnedDto, @MappingTarget Returned returned);
}