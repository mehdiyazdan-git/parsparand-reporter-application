package com.armaninvestment.parsparandreporterapplication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporterapplication.entities.User}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto implements Serializable {
    private Integer id;
    @Size(max = 255)
    private String email;
    @NotNull
    private Boolean enabled = false;
    @Size(max = 255)
    private String firstname;
    @Size(max = 255)
    private String lastname;
    @Size(max = 255)
    private String password;
    @Size(max = 255)
    private String role;
    @Size(max = 255)
    private String username;
    private Set<TokenDto> tokens = new LinkedHashSet<>();
}