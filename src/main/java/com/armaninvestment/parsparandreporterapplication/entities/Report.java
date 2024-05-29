package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "report")
public class Report{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Size(max = 255)
    @Column(name = "report_explanation")
    private String reportExplanation;

    @Column(name = "jalali_year")
    private Integer jalaliYear;

    @Column(name = "month")
    private Integer month;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id")
    private Year year;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReportItem> reportItems = new LinkedHashSet<>();

    public Double getTotalPrice(){
        return (reportItems.stream()
                .map(item -> item.getUnitPrice() * item.getQuantity() )
                .reduce(0d, Double::sum));
    }

    public Long getTotalQuantity(){
        return reportItems.stream()
                .mapToLong(ReportItem::getQuantity)
                .sum();
    }


}