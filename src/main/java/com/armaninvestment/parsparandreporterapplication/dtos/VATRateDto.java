package com.armaninvestment.parsparandreporterapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.VATRate}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VATRateDto implements Serializable {
    private Long id;
    private Float rate;
    private LocalDate effectiveFrom;
}