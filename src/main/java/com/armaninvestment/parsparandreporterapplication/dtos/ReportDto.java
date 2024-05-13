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
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Report}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDto implements Serializable {
    private Long id;
    private LocalDate reportDate;
    @Size(max = 255)
    private String reportExplanation;
    private Long yearId;
    private Set<ReportItemDto> reportItems = new LinkedHashSet<>();
}