package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReportDto;
import com.armaninvestment.parsparandreporterapplication.entities.Report;
import org.mapstruct.*;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ReportItemMapper.class})
public interface ReportMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(reportDto.getReportDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(reportDto.getReportDate()))")
    Report toEntity(ReportDto reportDto);

    @AfterMapping
    default void linkReportItems(@MappingTarget Report report) {
        report.getReportItems().forEach(reportItem -> reportItem.setReport(report));
    }

    @Mapping(source = "year.id", target = "yearId")
    @Mapping(expression = "java(getTotalPrice(report))", target = "totalPrice")
    @Mapping(expression = "java(getTotalQuantity(report))", target = "totalQuantity")
    ReportDto toDto(Report report);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(reportDto.getReportDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(reportDto.getReportDate()))")
    Report partialUpdate(ReportDto reportDto, @MappingTarget Report report);

    default Integer extractJalaliYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return jalaliDate.getYear();
    }

    default Integer extractMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return jalaliDate.getMonthPersian().getValue();
    }
    default Double getTotalPrice(Report report){
        return report.getTotalPrice();
    }
    default Long getTotalQuantity(Report report){
        return report.getTotalQuantity();
    }

}
