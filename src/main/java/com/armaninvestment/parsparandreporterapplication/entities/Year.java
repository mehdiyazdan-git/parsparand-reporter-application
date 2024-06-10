package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "year")
public class Year{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private Long name;

    @OneToMany(mappedBy = "year")
    private Set<Adjustment> adjustments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "year")
    private Set<Contract> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "year")
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "year")
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "year")
    private Set<Report> reports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "year")
    private Set<Returned> returneds = new LinkedHashSet<>();

    @OneToMany(mappedBy = "year")
    private Set<WarehouseReceipt> warehouseReceipts = new LinkedHashSet<>();

    public Year(Long name) {
        this.name = name;
    }

}