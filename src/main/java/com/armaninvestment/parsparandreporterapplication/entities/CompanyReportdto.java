package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "company_reportdto")
public class CompanyReportdto {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "avg_unit_price")
    private Long avgUnitPrice;

    @Column(name = "cumulative_total_amount")
    private Long cumulativeTotalAmount;

    @Column(name = "cumulative_total_quantity")
    private Long cumulativeTotalQuantity;

    @Size(max = 255)
    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "total_quantity")
    private Long totalQuantity;

}