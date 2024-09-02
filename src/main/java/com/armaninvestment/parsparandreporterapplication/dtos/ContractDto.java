package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
    private String contractDescription;
    private String contractNumber;
    private LocalDate endDate;
    private LocalDate startDate;
    private Long customerId;
    private String customerName;
    private Long yearId;
    private Double advancePayment;
    private Double insuranceDeposit;
    private Double performanceBond;
    private Long totalQuantity;
    private Double totalPrice;
    private List<ContractItemDto> contractItems = new ArrayList<>();
}