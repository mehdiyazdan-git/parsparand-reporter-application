package com.armaninvestment.parsparandreporterapplication.searchForms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class CustomerSearch implements Serializable {
    private Long id;
    private String name;
    private String phone;
    private String customerCode;
    private String economicCode;
    private String nationalCode;
    private Boolean bigCustomer;  // Change to Boolean to handle null values
}
