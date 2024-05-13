package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Adjustment}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdjustmentDto implements Serializable {
    private Long id;
    @Size(max = 255)
    private String adjustmentType;
    @Size(max = 255)
    private String description;
    private Long quantity;
    private Double unitPrice;
    private Long invoiceId;
    private LocalDate adjustmentDate;
    private Long adjustmentNumber;
}