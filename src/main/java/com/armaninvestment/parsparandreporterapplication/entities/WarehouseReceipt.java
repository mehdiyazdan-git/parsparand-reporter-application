package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@ToString
@RequiredArgsConstructor
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
    @ToString.Exclude
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id")
    @ToString.Exclude
    private Year year;

    @Column(name = "jalali_year")
    private Integer jalaliYear;

    @Column(name = "month")
    private Integer month;

    @OneToMany(mappedBy = "warehouseReceipt")
    @ToString.Exclude
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "warehouseReceipt")
    @ToString.Exclude
    private Set<ReportItem> reportItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "warehouseReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<WarehouseReceiptItem> warehouseReceiptItems = new LinkedHashSet<>();

    public void addInvoiceItem (InvoiceItem invoiceItem) {

        invoiceItems.add(invoiceItem);
        invoiceItem.setWarehouseReceipt(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        WarehouseReceipt that = (WarehouseReceipt) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}