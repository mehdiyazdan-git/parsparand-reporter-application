create function get_sales_by_year_group_by_month_filter_by_product_type(p_year integer, p_product_type integer)
    returns TABLE(month_number smallint, persian_month_name text, window_total_amount double precision, window_total_quantity bigint)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        WITH MonthlyReport AS (
            SELECT
                r.month AS reportMonth,
                get_persian_month_name(r.month) AS month_name,
                sum(ri.unit_price * ri.quantity) AS total_amount,
                sum(ri.quantity) AS total_quantity
            FROM
                report_item ri
                    JOIN report r ON r.id = ri.report_id
                    LEFT JOIN warehouse_receipt wr ON wr.id = ri.warehouse_receipt_id
                    LEFT JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
                    LEFT JOIN product p ON p.id = wri.product_id
                    LEFT JOIN customer c ON c.id = ri.customer_id
                    LEFT JOIN year y ON y.id = r.year_id
            where y.name = p_year
              and p.product_type = p_product_type
            GROUP BY
                reportMonth,month_name
        ), adjustments as (
            select
                a.month as adj_month,
                sum(a.unit_price * a.quantity * a.adjustment_type) as adjustment_amount
            from customer c
                     left join invoice i on i.customer_id = c.id
                     left join adjustment a on i.id = a.invoice_id
                     left join year y2 on i.year_id = y2.id
            where y2.name = p_year
            group by adj_month
        ),
             months AS (
                 SELECT generate_series(1, 12) as month_number
             )

        SELECT
            cast(reportMonth as smallint) as month_number,
            month_name as persian_month_name,
            total_amount + coalesce(cast(ad.adjustment_amount as double precision),0) as window_total_amount,
            coalesce(cast(total_quantity as bigint),0) as window_total_quantity
        FROM
            months
                left join  MonthlyReport mo on mo.reportMonth = months.month_number
                left join adjustments ad on ad.adj_month = months.month_number;
END;
$$;

alter function get_sales_by_year_group_by_month_filter_by_product_type(integer, integer) owner to postgres;

