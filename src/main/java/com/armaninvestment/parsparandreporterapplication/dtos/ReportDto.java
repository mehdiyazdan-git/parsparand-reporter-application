package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Report}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDto implements Serializable {
    private Long id;
    private LocalDate reportDate;
    private String reportExplanation;
    private Long yearId;
    private Double totalPrice;
    private Long totalQuantity;
    private Set<ReportItemDto> reportItems = new LinkedHashSet<>();

    public ReportDto(Long id, LocalDate reportDate, String reportExplanation, Long yearId, Double totalPrice, Long totalQuantity) {
        this.id = id;
        this.reportDate = reportDate;
        this.reportExplanation = reportExplanation;
        this.yearId = yearId;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
    }
}