package com.armaninvestment.parsparandreporterapplication.dtos;

import com.armaninvestment.parsparandreporterapplication.enums.PaymentSubject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class PaymentDto implements Serializable {
    private Long id;
    private LocalDate paymentDate;
    private String paymentDescription;
    private Long customerId;
    private String customerName;
    private Long yearId;
    private Long yearName;
    private Double paymentAmount;
    private PaymentSubject paymentSubject;
}