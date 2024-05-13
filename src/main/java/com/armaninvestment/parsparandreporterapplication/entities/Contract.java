package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "contracts")
public class Contract{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "contract_description")
    private String contractDescription;

    @Size(max = 255)
    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id")
    private Year year;

    @Column(name = "advance_payment")
    private Double advancePayment;

    @Column(name = "insurance_deposit")
    private Double insuranceDeposit;

    @Column(name = "performance_bond")
    private Double performanceBond;

    @OneToMany(mappedBy = "contract")
    private Set<Addendum> addenda = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<ContractItem> contractItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contract")
    private Set<Invoice> invoices = new LinkedHashSet<>();

}