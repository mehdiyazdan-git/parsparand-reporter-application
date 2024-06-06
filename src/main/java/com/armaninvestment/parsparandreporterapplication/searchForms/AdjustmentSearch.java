package com.armaninvestment.parsparandreporterapplication.searchForms;


import com.armaninvestment.parsparandreporterapplication.enums.AdjustmentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class AdjustmentSearch implements Serializable {
    private Long id;
    private Long adjustmentNumber;
    private LocalDate adjustmentDate;
    private String description;
    private Double unitPrice;
    private Integer quantity;
    private Long customerId;
    private AdjustmentType adjustmentType;
    private Long invoiceNumber;
    private Integer jalaliYear;
    private Double totalPrice;

}