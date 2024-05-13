package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "returned")
public class Returned{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Size(max = 255)
    @Column(name = "returned_description")
    private String returnedDescription;

    @Column(name = "returned_number")
    private Long returnedNumber;

    @Column(name = "unit_price")
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

}