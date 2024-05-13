package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.TokenDto;
import com.armaninvestment.parsparandreporterapplication.entities.Token;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TokenMapper {
    Token toEntity(TokenDto tokenDto);

    TokenDto toDto(Token token);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Token partialUpdate(TokenDto tokenDto, @MappingTarget Token token);
}