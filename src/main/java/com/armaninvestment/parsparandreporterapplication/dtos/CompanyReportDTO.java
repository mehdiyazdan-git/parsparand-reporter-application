package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyReportDTO implements Serializable {
    private String customerName;
    private BigDecimal totalAmount;
    private Long totalQuantity;
    private Long cumulativeTotalQuantity;
    private BigDecimal cumulativeTotalAmount;
    private BigDecimal avgUnitPrice;
}
