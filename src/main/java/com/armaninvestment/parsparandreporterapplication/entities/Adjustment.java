package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "adjustment")
public class Adjustment{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "adjustment_type")
    private String adjustmentType;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "adjustment_date")
    private LocalDate adjustmentDate;

    @Column(name = "adjustment_number")
    private Long adjustmentNumber;

}