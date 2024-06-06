package com.armaninvestment.parsparandreporterapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentReportDto {
    private Double productPayment;
    private Double insuranceDepositPayment;
    private Double performanceBoundPayment;
    private Double advancedPayment;
}
