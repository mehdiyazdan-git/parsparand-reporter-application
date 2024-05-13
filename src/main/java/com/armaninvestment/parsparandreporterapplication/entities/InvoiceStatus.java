package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "invoice_status")
public class InvoiceStatus{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "invoiceStatus")
    private Set<Invoice> invoices = new LinkedHashSet<>();

}