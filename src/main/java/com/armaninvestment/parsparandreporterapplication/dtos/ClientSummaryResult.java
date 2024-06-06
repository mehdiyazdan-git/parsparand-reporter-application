package com.armaninvestment.parsparandreporterapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientSummaryResult {
    private List<ClientSummaryDTO> clientSummaryList;
    private NotInvoicedReportDto notInvoicedReportDto;
    private AdjustmentReportDto adjustmentReportDto;
    private PaymentReportDto totalPaymentByCustomerId;
}
