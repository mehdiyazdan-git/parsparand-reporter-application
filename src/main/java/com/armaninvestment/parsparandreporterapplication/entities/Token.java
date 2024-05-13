package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "token")
public class Token {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "expired", nullable = false)
    private Boolean expired = false;

    @NotNull
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    @Size(max = 255)
    @Column(name = "token")
    private String token;

    @Size(max = 255)
    @Column(name = "token_type")
    private String tokenType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}