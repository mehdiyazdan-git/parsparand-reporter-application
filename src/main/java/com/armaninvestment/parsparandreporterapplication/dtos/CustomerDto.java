package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Customer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDto implements Serializable {
    private Long id;
    private Boolean monthlyReport;
    @Size(max = 255)
    private String customerCode;
    @Size(max = 255)
    private String economicCode;
    @Size(max = 255)
    private String name;
    @Size(max = 255)
    private String nationalCode;
    @Size(max = 255)
    private String phone;
}