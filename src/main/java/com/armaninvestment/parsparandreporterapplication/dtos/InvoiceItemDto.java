package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.InvoiceItem}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceItemDto implements Serializable {
    private Long id;
    private Long quantity;
    private Double unitPrice;
    private Long productId;
    private Long warehouseReceiptId;

    public Double getTotalPrice() {
        return quantity * unitPrice;
    }
}