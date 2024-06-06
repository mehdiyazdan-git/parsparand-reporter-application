package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.WarehouseReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseReceiptItemRepository extends JpaRepository<WarehouseReceiptItem, Long>, JpaSpecificationExecutor<WarehouseReceiptItem> {

    @Query(value = """
            with deposits as (
                select case
                           when i.sales_type = 'CONTRACTUAL_SALES' then c2.contract_number
                           when i.sales_type = 'CASH_SALES' then 'نقدی'
                           end                  as contract_number,
                       cast(coalesce(sum(I.advanced_payment),0) as double precision)  AS advanced_payment,
                       cast(coalesce(sum(I.performance_bound),0) as double precision) AS performance_bound,
                       cast(coalesce(sum(I.insurance_deposit),0) as double precision) AS insurance_deposit
                from invoice i
                         left join contracts c2 on c2.id = i.contract_id
                         join customer c on c.id = i.customer_id
                where i.customer_id = :customerId
                group by contract_number, i.sales_type
            ),
                sales_amount as (
                    select case
                               when i.sales_type = 'CONTRACTUAL_SALES' then c2.contract_number
                               when i.sales_type = 'CASH_SALES' then 'نقدی'
                               end as contract_number,
                           cast(coalesce(sum(ii.unit_price * ii.quantity),0) as double precision) as sales_amount,
                           cast(coalesce(sum(ii.quantity),0) as double precision) as sales_quantity,
                           cast(round(coalesce(sum(ii.unit_price * ii.quantity),0) * 0.09) as double precision) as vat
                    from invoice i
                             join invoice_item ii on i.id = ii.invoice_id
                             left join contracts c2 on c2.id = i.contract_id
                             join customer c on c.id = i.customer_id
                    where i.customer_id = :customerId
                    group by contract_number, i.sales_type
                )
            select coalesce(d.contract_number,s.contract_number),
                   coalesce(advanced_payment,0),
                   coalesce(performance_bound,0),
                   coalesce(insurance_deposit,0),
                   sales_amount,
                   sales_quantity,
                   vat
            from deposits d right outer join sales_amount s on d.contract_number = s.contract_number
            """, nativeQuery = true)
    List<Object[]> getClientSummaryByCustomerId(Long customerId);

    @Query(value = """
            select coalesce(sum(a.quantity * a.unit_price * a.adjustment_type),0)
            from adjustment a left join invoice i on i.id = a.invoice_id
            where i.customer_id = :customerId
            """,nativeQuery = true)
    Object getAdjustmentsByCustomerId(Long customerId);
}