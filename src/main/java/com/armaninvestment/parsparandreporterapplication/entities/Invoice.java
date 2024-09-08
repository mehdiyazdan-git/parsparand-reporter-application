package com.armaninvestment.parsparandreporterapplication.entities;

import com.armaninvestment.parsparandreporterapplication.enums.SalesType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @ManyToOne
    @JoinColumn(name = "vat_rate_id")
    private VATRate vatRate;

    @JoinColumn(name = "vat_amount")
    private Double vatAmount;

    @JoinColumn(name = "total_amount")
    private Double totalAmount;

    @Column(name = "total_amount_with_vat")
    private Double totalAmountWithVat;

    @Column(name = "sales_type", length = 20)
    @Enumerated(EnumType.STRING)
    private SalesType salesType;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private Customer customer;

    @OneToMany(mappedBy = "invoice")
    @ToString.Exclude
    private Set<Adjustment> adjustments = new LinkedHashSet<>();

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

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<InvoiceItem> invoiceItems = new ArrayList<>();

    @Column(name = "jalali_year")
    private Integer jalaliYear;

    @Column(name = "month")
    private Integer month;

    public Double calculateTotalAmount() {
        return invoiceItems
                .stream()
                .map(InvoiceItem::getTotalPrice)
                .mapToDouble(Double::doubleValue).sum();
    }
    public Double calculateTotalAmountWithVat() {
        return calculateTotalAmount() * (1 + getVatRate().getRate());
    }

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