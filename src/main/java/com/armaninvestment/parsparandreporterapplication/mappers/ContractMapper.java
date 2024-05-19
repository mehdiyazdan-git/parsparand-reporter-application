package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ContractDto;
import com.armaninvestment.parsparandreporterapplication.dtos.ContractSelectDto;
import com.armaninvestment.parsparandreporterapplication.entities.Contract;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ContractItemMapper.class})
public interface ContractMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "customerId", target = "customer.id")
    Contract toEntity(ContractDto contractDto);

    @AfterMapping
    default void linkContractItems(@MappingTarget Contract contract) {
        contract.getContractItems().forEach(contractItem -> contractItem.setContract(contract));
    }

    @InheritInverseConfiguration(name = "toEntity")
    ContractDto toDto(Contract contract);

    ContractSelectDto toSelectDto(Contract contract);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Contract partialUpdate(ContractDto contractDto, @MappingTarget Contract contract);
}