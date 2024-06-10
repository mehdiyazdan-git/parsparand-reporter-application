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
public class WarehouseReceiptSearch {
    private Long id;
    private Long warehouseReceiptNumber;
    private LocalDate warehouseReceiptDate;
    private String warehouseReceiptDescription;
    private String customerName;
    private Long customerId;
    private Boolean notInvoiced;
    private Integer jalaliYear;
    private Long totalQuantity;
    private Double totalPrice;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String order;


}