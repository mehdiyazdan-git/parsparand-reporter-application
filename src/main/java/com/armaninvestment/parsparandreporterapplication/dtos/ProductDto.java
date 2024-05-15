package com.armaninvestment.parsparandreporterapplication.dtos;

import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Product}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto implements Serializable {
    private Long id;
    @NotNull
    @Size(max = 255)
    private String measurementIndex;
    @Size(max = 255)
    private String productCode;
    @Size(max = 255)
    private String productName;
    @NotNull
    private ProductType productType;
}