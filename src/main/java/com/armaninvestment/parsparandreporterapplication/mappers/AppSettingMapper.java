package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.AppSettingDto;
import com.armaninvestment.parsparandreporterapplication.entities.AppSetting;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppSettingMapper {
    AppSetting toEntity(AppSettingDto appSettingDto);

    AppSettingDto toDto(AppSetting appSetting);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AppSetting partialUpdate(AppSettingDto appSettingDto, @MappingTarget AppSetting appSetting);
}