package com.armaninvestment.parsparandreporterapplication.searchForms;

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
public class PaymentSearch implements Serializable {
    private Long id;
    private String description;
    private LocalDate date;
    private Long amount;
    private String subject;
    private String customerName;
    private Long yearName;
}