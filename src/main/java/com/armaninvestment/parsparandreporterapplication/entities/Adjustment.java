package com.armaninvestment.parsparandreporterapplication.entities;

import com.armaninvestment.parsparandreporterapplication.enums.AdjustmentType;
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

    @Column(name = "adjustment_type")
    private AdjustmentType adjustmentType;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.MERGE})
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "adjustment_date")
    private LocalDate adjustmentDate;

    @Column(name = "adjustment_number")
    private Long adjustmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id")
    private Year year;

    @Column(name = "jalali_year")
    private Integer jalaliYear;

    @Column(name = "month")
    private Integer month;
}