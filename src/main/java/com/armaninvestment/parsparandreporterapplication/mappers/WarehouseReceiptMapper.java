package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt;
import org.mapstruct.*;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;

import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {WarehouseReceiptItemMapper.class})
public interface WarehouseReceiptMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(warehouseReceiptDto.getWarehouseReceiptDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(warehouseReceiptDto.getWarehouseReceiptDate()))")
    WarehouseReceipt toEntity(WarehouseReceiptDto warehouseReceiptDto);

    @AfterMapping
    default void linkWarehouseReceiptItems(@MappingTarget WarehouseReceipt warehouseReceipt) {
        warehouseReceipt.getWarehouseReceiptItems().forEach(warehouseReceiptItem -> warehouseReceiptItem.setWarehouseReceipt(warehouseReceipt));
    }

    @Mapping(source = "year.id", target = "yearId")
    @Mapping(source = "year.name", target = "yearName")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    WarehouseReceiptDto toDto(WarehouseReceipt warehouseReceipt);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(warehouseReceiptDto.getWarehouseReceiptDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(warehouseReceiptDto.getWarehouseReceiptDate()))")
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "customer", ignore = true)
    WarehouseReceipt partialUpdate(WarehouseReceiptDto warehouseReceiptDto, @MappingTarget WarehouseReceipt warehouseReceipt);

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
