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
@Table(name = "invoice")
public class Invoice{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "invoice_number")
    private Long invoiceNumber;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Size(max = 20)
    @Column(name = "sales_type", length = 20)
    private String salesType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_status_id")
    private InvoiceStatus invoiceStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id")
    private Year year;

    @Column(name = "advanced_payment")
    private Long advancedPayment;

    @Column(name = "insurance_deposit")
    private Long insuranceDeposit;

    @Column(name = "performance_bound")
    private Long performanceBound;

    @OneToMany(mappedBy = "invoice")
    private Set<Adjustment> adjustments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "invoice")
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<>();

    @Column(name = "jalali_year")
    private Integer jalaliYear;

    @Column(name = "month")
    private Integer month;

}