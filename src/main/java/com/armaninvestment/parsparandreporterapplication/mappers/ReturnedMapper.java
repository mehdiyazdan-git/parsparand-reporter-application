package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporterapplication.entities.Returned;
import org.mapstruct.*;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;

import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReturnedMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(target = "customer" , ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(returnedDto.getReturnedDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(returnedDto.getReturnedDate()))")
    Returned toEntity(ReturnedDto returnedDto);

    @Mapping(source = "year.id", target = "yearId")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    ReturnedDto toDto(Returned returned);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "customer" , ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(returnedDto.getReturnedDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(returnedDto.getReturnedDate()))")
    Returned partialUpdate(ReturnedDto returnedDto, @MappingTarget Returned returned);

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
}
