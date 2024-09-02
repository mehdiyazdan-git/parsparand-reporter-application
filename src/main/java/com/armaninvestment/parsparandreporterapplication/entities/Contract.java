package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
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
    private List<Addendum> addenda = new ArrayList<>();

    @OneToMany(mappedBy = "contract",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractItem> contractItems = new ArrayList<>();

    @OneToMany(mappedBy = "contract")
    private List<Invoice> invoices = new ArrayList<>();
}