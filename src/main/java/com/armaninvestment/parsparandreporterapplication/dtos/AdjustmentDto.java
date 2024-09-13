package com.armaninvestment.parsparandreporterapplication.dtos;

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
public class AdjustmentDto implements Serializable {
    private Long id;
    private AdjustmentType adjustmentType;
    private String description;
    private Long quantity;
    private Double unitPrice;
    private Double totalPrice;
    private Long invoiceId;
    private Long invoiceNumber;
    private LocalDate adjustmentDate;
    private Long adjustmentNumber;
    private Long yearId;
}

//public enum AdjustmentType {
//    POSITIVE,
//    NEGATIVE;

//                            adjustmentType: 'POSITIVE',
//                            description: '',
//                            quantity: '',
//                            unitPrice: '',
//                            invoiceId: '',
//                            adjustmentDate: '',
//                            adjustmentNumber: '',