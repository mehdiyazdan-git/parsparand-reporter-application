create function search_warehouse_receipt_by_description_keywords(search_term text, fiscal_year_id bigint)
    returns TABLE(id bigint, description text)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        SELECT i.id, i.description
        FROM (SELECT wwc.id,
                     CONCAT('حواله ', wwc.receiptNo, '-', wwc.customerName, ' تاریخ ', wwc.receiptDate, ' تعداد',
                            wwc.quantity) AS description
              FROM (SELECT wr.id,
                           wr.warehouse_receipt_number                     AS receiptNo,
                           gregorian_to_persian(wr.warehouse_receipt_date) AS receiptDate,
                           c.name                                          AS customerName,
                           SUM(wri.quantity)                               AS quantity
                    FROM warehouse_receipt wr
                             JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
                             JOIN customer c ON c.id = wr.customer_id
                    WHERE wr.year_id is null or wr.year_id = fiscal_year_id
                    GROUP BY wr.id, receiptNo, receiptDate, customerName) wwc) i
        WHERE i.description ILIKE '%' || search_term || '%';

    RETURN;
END;
$$;

alter function search_warehouse_receipt_by_description_keywords(text, bigint) owner to postgres;