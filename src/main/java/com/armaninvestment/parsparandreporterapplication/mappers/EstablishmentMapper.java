package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.EstablishmentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Establishment;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface EstablishmentMapper {
    @Mapping(source = "customerId", target = "customer.id")
    Establishment toEntity(EstablishmentDto establishmentDto);

    @Mapping(source = "customer.id", target = "customerId")
    EstablishmentDto toDto(Establishment establishment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "customerId", target = "customer.id")
    Establishment partialUpdate(EstablishmentDto establishmentDto, @MappingTarget Establishment establishment);
}