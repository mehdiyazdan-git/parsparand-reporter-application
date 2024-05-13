package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "measurement_index", nullable = false)
    private String measurementIndex;

    @Size(max = 255)
    @Column(name = "product_code")
    private String productCode;

    @Size(max = 255)
    @Column(name = "product_name")
    private String productName;

    @OneToMany(mappedBy = "product")
    private Set<ContractItem> contractItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<WarehouseReceiptItem> warehouseReceiptItems = new LinkedHashSet<>();

}