package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ProductDto;
import com.armaninvestment.parsparandreporterapplication.entities.Product;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    Product toEntity(ProductDto productDto);

    ProductDto toDto(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Product partialUpdate(ProductDto productDto, @MappingTarget Product product);
}