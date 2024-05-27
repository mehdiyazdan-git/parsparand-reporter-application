package com.armaninvestment.parsparandreporterapplication.entities;

import com.armaninvestment.parsparandreporterapplication.enums.ProductType;
import com.armaninvestment.parsparandreporterapplication.enums.ProductTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
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
    @ToString.Exclude
    private Set<ContractItem> contractItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    private Set<WarehouseReceiptItem> warehouseReceiptItems = new LinkedHashSet<>();

    public Product(String productName, ProductType productType) {
        this.productName = productName;
        this.productType = productType;
    }
    public void addContractItem(ContractItem contractItem) {
        contractItems.add(contractItem);
        contractItem.setProduct(this);
    }

    public void addInvoiceItem(InvoiceItem invoiceItem) {
        invoiceItems.add(invoiceItem);
        invoiceItem.setProduct(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Product product = (Product) o;
        return getId() != null && Objects.equals(getId(), product.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
