package com.armaninvestment.parsparandreporterapplication.searchForms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseReceiptSearch implements Serializable {
    private Long id;
    private Long warehouseReceiptNumber;
    private LocalDate warehouseReceiptDate;
    private String warehouseReceiptDescription;
    private String customerName;
    private Long yearName;
}