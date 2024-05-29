package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.ReportItem}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportItemDto implements Serializable {
    private Long id;
    private Long quantity;
    private Double unitPrice;
    private Long customerId;
    private Long warehouseReceiptId;
}