package com.armaninvestment.parsparandreporterapplication.searchForms;

import com.armaninvestment.parsparandreporterapplication.enums.PaymentSubject;
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
    private String paymentDescription;
    private Long customerId;
    private String customerName;
    private Integer jalaliYear;
    private Long yearName;
    private Double paymentAmount;
    private PaymentSubject paymentSubject;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String order;
}