package com.armaninvestment.parsparandreporterapplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import jdk.jshell.Snippet;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "customer")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "big_customer")
    private Boolean bigCustomer = false;

    @Size(max = 255)
    @Column(name = "customer_code")
    private String customerCode;

    @Size(max = 255)
    @Column(name = "economic_code")
    private String economicCode;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Column(name = "national_code")
    private String nationalCode;

    @Size(max = 255)
    @Column(name = "phone")
    private String phone;

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<Contract> contracts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<Establishment> establishments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<ReportItem> reportItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<Returned> returneds = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<WarehouseReceipt> warehouseReceipts = new LinkedHashSet<>();

    public Customer(String name) {
        this.name = name;

    }

    public Customer(Long id) {
        this.id = id;
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoice.setCustomer(this);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setCustomer(this);
    }

    public void addReportItem(ReportItem reportItem) {
        reportItems.add(reportItem);
        reportItem.setCustomer(this);
    }

    public void addReturned(Returned returned) {
        returneds.add(returned);
        returned.setCustomer(this);
    }

    public void addWarehouseReceipt(WarehouseReceipt warehouseReceipt) {
        warehouseReceipts.add(warehouseReceipt);
        warehouseReceipt.setCustomer(this);
    }

    public void addContract(Contract contract) {
        contracts.add(contract);
        contract.setCustomer(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Customer customer = (Customer) o;
        return getId() != null && Objects.equals(getId(), customer.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}