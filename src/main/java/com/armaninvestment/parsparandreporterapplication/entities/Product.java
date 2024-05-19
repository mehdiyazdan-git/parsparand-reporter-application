package com.armaninvestment.parsparandreporterapplication.entities;

import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.armaninvestment.parsparandreporterapplication.enums.ProductTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "product")
@RequiredArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Size(max = 255)
    @Column(name = "measurement_index")
    private String measurementIndex;

    @Size(max = 255)
    @Column(name = "product_code")
    private String productCode;

    @Size(max = 255)
    @Column(name = "product_name")
    private String productName;

    @Convert(converter = ProductTypeConverter.class)
    @Column(name = "product_type")
    private ProductType productType;

    @OneToMany(mappedBy = "product")
    private Set<ContractItem> contractItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<WarehouseReceiptItem> warehouseReceiptItems = new LinkedHashSet<>();

    public Product(String productName, ProductType productType) {
        this.productName = productName;
        this.productType = productType;
    }
}
