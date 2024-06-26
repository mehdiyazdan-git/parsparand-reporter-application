package com.armaninvestment.parsparandreporterapplication.searchForms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Report}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportSearch implements Serializable {
    private Long id;
    private String reportExplanation;
    private LocalDate reportDate;
    private Integer JalaliYear;
    private Double totalPrice;
    private Long totalQuantity;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String order;
}