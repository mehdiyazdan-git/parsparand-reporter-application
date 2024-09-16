package com.armaninvestment.parsparandreporterapplication.mappers;

import com.armaninvestment.parsparandreporterapplication.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporterapplication.entities.Invoice;
import com.armaninvestment.parsparandreporterapplication.entities.InvoiceSelectDto;
import com.armaninvestment.parsparandreporterapplication.entities.VATRate;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,uses = {InvoiceItemMapper.class})
public interface InvoiceMapper {

    @Mapping(source = "vatRateId", target = "vatRate.id")
    @Mapping(source = "invoiceStatusId", target = "invoiceStatus.id")
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(invoiceDto.getIssuedDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(invoiceDto.getIssuedDate()))")
    @Mapping(target = "vatAmount", expression = "java(calculateVAT(invoiceDto.calculateTotalAmount()))")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(source = "yearId", target = "year.id")
    Invoice toEntity(InvoiceDto invoiceDto);

    @Mapping(source = "vatRate.id", target = "vatRateId")
    @Mapping(source = "year.id", target = "yearId")
    @Mapping(source = "invoiceStatus.id", target = "invoiceStatusId")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "contract.id", target = "contractId")
    @Mapping(source = "contract.contractNumber", target = "contractNumber")
    InvoiceDto toDto(Invoice invoice);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "invoiceStatusId", target = "invoiceStatus.id")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(target = "jalaliYear", expression = "java(extractJalaliYear(invoiceDto.getIssuedDate()))")
    @Mapping(target = "month", expression = "java(extractMonth(invoiceDto.getIssuedDate()))")
    @Mapping(target = "vatAmount", expression = "java(calculateVAT(invoiceDto.calculateTotalAmount()))")
    Invoice partialUpdate(InvoiceDto invoiceDto, @MappingTarget Invoice invoice);




    @AfterMapping
    default void linkInvoiceItems(@MappingTarget Invoice invoice) {
        invoice.getInvoiceItems().forEach(invoiceItem -> invoiceItem.setInvoice(invoice));
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
    default Double calculateVAT(Double totalAmount) {
        if (totalAmount == null) {
            return null;
        }
        return totalAmount * 0.09; // Assuming VAT is 9%
    }
}
