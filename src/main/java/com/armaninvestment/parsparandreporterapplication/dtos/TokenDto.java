package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.Token}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenDto implements Serializable {
    private Integer id;
    @NotNull
    private Boolean expired = false;
    @NotNull
    private Boolean revoked = false;
    @Size(max = 255)
    private String token;
    @Size(max = 255)
    private String tokenType;
}