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
@Table(name = "warehouse_receipt")
public class WarehouseReceipt{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "warehouse_receipt_date")
    private LocalDate warehouseReceiptDate;

    @Size(max = 255)
    @Column(name = "warehouse_receipt_description")
    private String warehouseReceiptDescription;

    @Column(name = "warehouse_receipt_number")
    private Long warehouseReceiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id")
    private Year year;

    @OneToMany(mappedBy = "warehouseReceipt")
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "warehouseReceipt")
    private Set<ReportItem> reportItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "warehouseReceipt")
    private Set<WarehouseReceiptItem> warehouseReceiptItems = new LinkedHashSet<>();

}