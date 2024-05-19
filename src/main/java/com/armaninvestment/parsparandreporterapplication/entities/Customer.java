package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "customer")
@RequiredArgsConstructor
public class Customer{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "monthly_report")
    private Boolean monthlyReport;

    @Size(max = 255)
    @Column(name = "customer_code")
    private String customerCode;

    @Size(max = 255)
    @Column(name = "economic_code")
    private String economicCode;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Column(name = "national_code")
    private String nationalCode;

    @Size(max = 255)
    @Column(name = "phone")
    private String phone;

    @OneToMany(mappedBy = "customer")
    private Set<Contract> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Establishment> establishments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<ReportItem> reportItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Returned> returneds = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<WarehouseReceipt> warehouseReceipts = new LinkedHashSet<>();

    public Customer(String name) {
        this.name = name;

    }
}