package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "warehouse_invoice")
public class WarehouseInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "invoice_id")
    private Long invoiceId;

}