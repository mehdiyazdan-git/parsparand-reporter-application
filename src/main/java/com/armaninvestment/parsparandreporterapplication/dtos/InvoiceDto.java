package com.armaninvestment.parsparandreporterapplication.dtos;

import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Invoice}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceDto implements Serializable {
    private Long id;
    private LocalDate dueDate;
    private Long invoiceNumber;
    private LocalDate issuedDate;
    @Enumerated(EnumType.STRING)
    private SalesType salesType;
    private Long contractId;
    private String contractNumber;
    private Long customerId;
    private String customerName;
    private Integer invoiceStatusId;
    private Long advancedPayment;
    private Long insuranceDeposit;
    private Long performanceBound;
    private Long yearId;
    private Long totalQuantity;
    private Double totalPrice;
    private List<InvoiceItemDto> invoiceItems = new ArrayList<>();

    public Double calculateTotalAmount() {
        return invoiceItems.stream().mapToDouble(InvoiceItemDto::getTotalPrice).sum();
    }
}