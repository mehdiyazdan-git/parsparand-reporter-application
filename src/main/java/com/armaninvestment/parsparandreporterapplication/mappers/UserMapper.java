package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.UserDto;
import com.armaninvestment.parsparandreporterapplication.entities.User;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TokenMapper.class})
public interface UserMapper {
    User toEntity(UserDto userDto);

    @AfterMapping
    default void linkTokens(@MappingTarget User user) {
        user.getTokens().forEach(token -> token.setUser(user));
    }

    UserDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserDto userDto, @MappingTarget User user);
}