package com.armaninvestment.parsparandreporterapplication.dtos;

import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

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
    @Size(max = 20)
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

    private Set<InvoiceItemDto> invoiceItems = new LinkedHashSet<>();
}