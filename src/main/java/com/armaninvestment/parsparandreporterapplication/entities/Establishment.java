package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "establishment")
public class Establishment{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "claims")
    private Double claims;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "perior_insurance_deposit")
    private Double periorInsuranceDeposit;

    @Column(name = "perior_performance_bound")
    private Double periorPerformanceBound;

}