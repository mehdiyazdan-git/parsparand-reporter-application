package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReportDto;
import com.armaninvestment.parsparandreporterapplication.entities.Report;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ReportItemMapper.class})
public interface ReportMapper {
    @Mapping(source = "yearId", target = "year.id")
    Report toEntity(ReportDto reportDto);

    @AfterMapping
    default void linkReportItems(@MappingTarget Report report) {
        report.getReportItems().forEach(reportItem -> reportItem.setReport(report));
    }

    @Mapping(source = "year.id", target = "yearId")
    ReportDto toDto(Report report);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "yearId", target = "year.id")
    Report partialUpdate(ReportDto reportDto, @MappingTarget Report report);
}