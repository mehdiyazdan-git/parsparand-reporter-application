package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "_user")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Size(max = 255)
    @Column(name = "firstname")
    private String firstname;

    @Size(max = 255)
    @Column(name = "lastname")
    private String lastname;

    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @Size(max = 255)
    @Column(name = "role")
    private String role;

    @Size(max = 255)
    @Column(name = "username")
    private String username;

    @OneToMany(mappedBy = "user")
    private Set<Token> tokens = new LinkedHashSet<>();

}