package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "addendum")
public class Addendum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "addendum_number")
    private String addendumNumber;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "unit_price")
    private Long unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id")
    private Year year;

}