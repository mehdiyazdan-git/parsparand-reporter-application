package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseReceiptDto implements Serializable {
    private Long id;
    private LocalDate warehouseReceiptDate;
    private String warehouseReceiptDescription;
    private Long warehouseReceiptNumber;
    private Long customerId;
    private String customerName;
    private Long yearId;
    private Long yearName;
    private Long totalQuantity;
    private Double totalPrice;
    private List<WarehouseReceiptItemDto> warehouseReceiptItems = new ArrayList<>();
}
