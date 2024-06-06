package com.armaninvestment.parsparandreporterapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientSummaryDTO {
    private String contractNumber;
    private Double advancedPayment;
    private Double performanceBound;
    private Double insuranceDeposit;
    private Double salesAmount;
    private Double salesQuantity;
    private Double vat;
}