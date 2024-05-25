package com.armaninvestment.parsparandreporterapplication.searchForms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Contract}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractSearch implements Serializable {
    private Long id;
    private String contractNumber;
    private String contractDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double advancePayment;
    private Double performanceBond;
    private Double insuranceDeposit;
    private String customerName;
    private Integer jalaliYear;
}