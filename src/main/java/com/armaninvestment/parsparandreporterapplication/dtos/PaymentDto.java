package com.armaninvestment.parsparandreporterapplication.dtos;

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
public class PaymentDto implements Serializable {
    private Long id;
    private LocalDate paymentDate;
    @Size(max = 255)
    private String paymentDescryption;
    private Long customerId;
    private Long yearId;
    private Long paymentAmount;
    @Size(max = 255)
    private String paymentSubject;
}