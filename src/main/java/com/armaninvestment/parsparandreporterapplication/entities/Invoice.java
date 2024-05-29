package com.armaninvestment.parsparandreporterapplication.entities;

import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import jakarta.persistence.*;
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
@Table(name = "invoice")
public class Invoice{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "invoice_number")
    private Long invoiceNumber;

    @Column(name = "issued_date")
    private LocalDate issuedDate;


    @Column(name = "sales_type", length = 20)
    @Enumerated(EnumType.STRING)
    private SalesType salesType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "contract_id")
    @ToString.Exclude
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "invoice_status_id")
    @ToString.Exclude
    private InvoiceStatus invoiceStatus;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "year_id")
    @ToString.Exclude
    private Year year;

    @Column(name = "advanced_payment")
    private Long advancedPayment;

    @Column(name = "insurance_deposit")
    private Long insuranceDeposit;

    @Column(name = "performance_bound")
    private Long performanceBound;

    @OneToMany(mappedBy = "invoice")
    @ToString.Exclude
    private Set<Adjustment> adjustments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<InvoiceItem> invoiceItems = new LinkedHashSet<>();

    @Column(name = "jalali_year")
    private Integer jalaliYear;

    @Column(name = "month")
    private Integer month;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Invoice invoice = (Invoice) o;
        return getId() != null && Objects.equals(getId(), invoice.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}