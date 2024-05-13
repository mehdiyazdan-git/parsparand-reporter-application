package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
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
    @Size(max = 255)
    private String warehouseReceiptDescription;
    private Long warehouseReceiptNumber;
    private Long customerId;
    private Long yearId;
    private Set<WarehouseReceiptItemDto> warehouseReceiptItems = new LinkedHashSet<>();
}