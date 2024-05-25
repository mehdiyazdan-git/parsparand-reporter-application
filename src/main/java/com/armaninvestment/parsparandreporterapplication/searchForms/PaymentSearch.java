package com.armaninvestment.parsparandreporterapplication.searchForms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Payment}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentSearch implements Serializable {
    private Long id;
    private LocalDate paymentDate;
    private String paymentDescryption;
    private Long customerId;
    private String customerName;
    private Integer jalaliYear;
    private Long yearName;
    private Long paymentAmount;
    private String paymentSubject;
}