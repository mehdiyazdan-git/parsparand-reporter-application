package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporterapplication.entities.Adjustment;
import org.mapstruct.*;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;

import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdjustmentMapper {
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(adjustmentDto.getAdjustmentDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(adjustmentDto.getAdjustmentDate()))")
    Adjustment toEntity(AdjustmentDto adjustmentDto);

    @Mapping(source = "year.id", target = "yearId")
    @Mapping(source = "invoice.id", target = "invoiceId")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(adjustment.getUnitPrice(), adjustment.getQuantity()))")
    AdjustmentDto toDto(Adjustment adjustment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "invoiceId", target = "invoice.id")
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(adjustmentDto.getAdjustmentDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(adjustmentDto.getAdjustmentDate()))")
    Adjustment partialUpdate(AdjustmentDto adjustmentDto, @MappingTarget Adjustment adjustment);



    default Double calculateTotalPrice(Double unitPrice, Integer quantity) {
        return unitPrice * quantity;
    }
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
