package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.ContractItem}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractItemDto implements Serializable {
    private Long id;
    private Long quantity;
    private Long unitPrice;
    private Long productId;
}