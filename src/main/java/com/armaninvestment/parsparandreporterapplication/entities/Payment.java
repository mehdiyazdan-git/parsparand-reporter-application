package com.armaninvestment.parsparandreporterapplication.entities;

import com.armaninvestment.parsparandreporterapplication.enums.PaymentSubject;
import com.armaninvestment.parsparandreporterapplication.enums.PaymentSubjectConverter;
import com.armaninvestment.parsparandreporterapplication.enums.ProductTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class Payment{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Size(max = 255)
    @Column(name = "payment_description")
    private String paymentDescription;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "year_id")
    private Year year;

    @Column(name = "payment_amount")
    private Double paymentAmount;

    @Convert(converter = PaymentSubjectConverter.class)
    @Column(name = "payment_subject")
    private PaymentSubject paymentSubject;

    @Column(name = "jalali_year")
    private Integer jalaliYear;

    @Column(name = "month")
    private Integer month;
}