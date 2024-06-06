package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyReportDTO implements Serializable {
    @Id
    private Long id;
    private String customerName;
    private Long totalAmount;
    private Long totalQuantity;
    private Long cumulativeTotalQuantity;
    private Long cumulativeTotalAmount;
    private Long avgUnitPrice;
}
