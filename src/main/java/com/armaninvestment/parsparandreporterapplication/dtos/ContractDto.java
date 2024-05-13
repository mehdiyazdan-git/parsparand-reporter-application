package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Contract}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractDto implements Serializable {
    private Long id;
    @Size(max = 255)
    private String contractDescription;
    @Size(max = 255)
    private String contractNumber;
    private LocalDate endDate;
    private LocalDate startDate;
    private Long customerId;
    private Long yearId;
    private Double advancePayment;
    private Double insuranceDeposit;
    private Double performanceBond;
    private Set<ContractItemDto> contractItems = new LinkedHashSet<>();
}