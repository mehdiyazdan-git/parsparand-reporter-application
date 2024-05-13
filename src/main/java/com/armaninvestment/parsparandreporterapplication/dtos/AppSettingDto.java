package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.AppSetting}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSettingDto implements Serializable {
    private Long id;
    private Double vat;
}