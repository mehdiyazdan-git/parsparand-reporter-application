package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceipt}
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseReceiptSelect implements Serializable {
    private Long id;
    private String name;

    public WarehouseReceiptSelect() {
    }
}