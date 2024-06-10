select
    invoice_number,
    gregorian_to_persian(issued_date) as issuedDate,
    gregorian_to_persian(due_date) as dueDate,
    sales_type,
    c.contract_number,
    cu.customer_code,
    advanced_payment,
    invoice.insurance_deposit,
    performance_bound,
    y.name as yearNane,
    ii.quantity,
    ii.unit_price,
    p.product_code,
    wr.warehouse_receipt_number,
    gregorian_to_persian(wr.warehouse_receipt_date) as warehouseReceiptDate,
    s.id as statusId
from invoice
         left join contracts c on invoice.contract_id = c.id
         left join customer cu on invoice.customer_id = cu.id
         left join invoice_item ii on invoice.id = ii.invoice_id
         left join product p on ii.product_id = p.id
         left join warehouse_receipt wr on ii.warehouse_receipt_id = wr.id
         left join invoice_status s on invoice.invoice_status_id = s.id
         left join year y on invoice.year_id = y.id;