select
    wr.warehouse_receipt_number as warehouseReceiptNumber,
    coalesce(gregorian_to_persian(wr.warehouse_receipt_date),'') as warehouseReceiptDate,
    wr.warehouse_receipt_description as warehouseReceiptDescription,
    c.customer_code customerCode,
    y.name as yearName,
    wri.quantity,
    wri.unit_price as unitPrice,
    p.product_code as productCode
from warehouse_receipt_item wri
         left join public.product p on p.id = wri.product_id
         left join public.warehouse_receipt wr on wr.id = wri.warehouse_receipt_id
         left outer join public.year y on y.id = wr.year_id
         left join public.customer c on c.id = wr.customer_id;