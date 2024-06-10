package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Returned}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnedDto implements Serializable {
    private Long id;
    private Long quantity;
    private Double unitPrice;
    private LocalDate returnedDate;
    private String returnedDescription;
    private Long returnedNumber;
    private Long customerId;
    private String customerName;
    private Integer jalaliYear;
    private Long yearId;


}