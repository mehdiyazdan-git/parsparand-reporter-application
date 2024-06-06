create or replace function get_monthly_report_by_year_and_month(p_year integer, p_month integer, p_product_type text)
    returns TABLE(temp_customer_id bigint, temp_customer_name character varying, current_total_quantity bigint, current_total_amount bigint, cumulativetotalquantity bigint, cumulativetotalamount bigint, avg bigint)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        with current as (select c.id                                          as customer_id,
                                c.name                                        as customer_name,
                                coalesce(sum(ri.quantity), 0)                 as total_quantity,
                                coalesce(sum(ri.quantity * ri.unit_price), 0) as total_amount
                         from report_item ri
                                  join report r on r.id = ri.report_id
                                  join customer c on c.id = ri.customer_id
                                  join year y on y.id = r.year_id
                         where get_persian_month(gregorian_to_persian(r.report_date + INTERVAL '1 DAY')) = p_month
                           and y.name = p_year
                           and c.big_customer = true
                         group by customer_name, c.id

                         union all
                         select 0                                             as customer_id,
                                'سایر'                                        as customer_name,
                                coalesce(sum(ri.quantity), 0)                 as total_quantity,
                                coalesce(sum(ri.quantity * ri.unit_price), 0) as total_amount
                         from report_item ri
                                  join report r on r.id = ri.report_id
                                  join customer c on c.id = ri.customer_id
                                  join year y on y.id = r.year_id
                         where get_persian_month(gregorian_to_persian(r.report_date + INTERVAL '1 DAY')) = p_month
                           and y.name = p_year
                           and c.big_customer = false
                         group by customer_name),
             cumulative as (select c.id                                          as customer_id,
                                   c.name                                        as customer_name,
                                   coalesce(sum(ri.quantity), 0)                 as total_quantity,
                                   coalesce(sum(ri.quantity * ri.unit_price), 0) as total_amount
                            from report_item ri
                                     join report r on r.id = ri.report_id
                                     join customer c on c.id = ri.customer_id
                                     join year y on y.id = r.year_id
                            where get_persian_month(gregorian_to_persian(r.report_date + INTERVAL '1 DAY')) <= p_month
                              and y.name = p_year
                              and c.big_customer = true
                            group by customer_name, c.id

                            union all
                            select 0                                             as customer_id,
                                   'سایر'                                        as customer_name,
                                   coalesce(sum(ri.quantity), 0)                 as total_quantity,
                                   coalesce(sum(ri.quantity * ri.unit_price), 0) as total_amount
                            from report_item ri
                                     join report r on r.id = ri.report_id
                                     join customer c on c.id = ri.customer_id
                                     join year y on y.id = r.year_id
                            where get_persian_month(gregorian_to_persian(r.report_date + INTERVAL '1 DAY')) <= p_month
                              and y.name = p_year
                              and c.big_customer = false
                            group by customer_name),
             customers as (select c.id, c.name
                           from customer c
                           where big_customer = true
                           union all
                           select 0, 'سایر'),
             adjustment as (select CASE
                                       WHEN big_customer = TRUE THEN c.id
                                       WHEN big_customer = FALSE THEN 0
                                       end  as client_id,
                                   CASE
                                       WHEN big_customer = TRUE THEN c.name
                                       WHEN big_customer = FALSE THEN 'سایر'
                                       end  as client_name,
                                   sum(a.unit_price * a.quantity * adjustment_type) as adjustment_amount
                            from customer c
                                     left join invoice i on i.customer_id = c.id
                                     left join adjustment a on i.id = a.invoice_id
                                     left join year y2 on i.year_id = y2.id
                            where y2.name = p_year
                            group by c.big_customer, client_id, client_name)

        select c.id                                                                                            as temp_customer_id,
               c.name                                                                                          as temp_customer_name,
               coalesce(sum(cu.total_quantity), 0)::bigint                                                     as current_total_quantity,
               coalesce(sum(cu.total_amount), 0)::bigint                                                       as current_total_amount,
               coalesce(sum(cum.total_quantity), 0)::bigint                                                    as cumulativetotalquantity,
               coalesce(coalesce(sum(cum.total_amount), 0) + coalesce(sum(a.adjustment_amount), 0),
                        0)::bigint                                                                             as cumulativetotalamount,
               ROUND(
                       CASE
                           WHEN sum(cu.total_quantity) > 0
                               THEN
                                       COALESCE(SUM(cu.total_amount), + SUM(cum.total_amount) + coalesce(SUM(a.adjustment_amount),0), 0)
                                       /
                                       SUM(cu.total_quantity) + SUM(cum.total_quantity)
                           ELSE
                                   COALESCE(SUM(cum.total_amount) + coalesce(SUM(a.adjustment_amount),0), 0)
                                   /
                                   SUM(cum.total_quantity)
                           END
                   )::BIGINT  AS avg
        from customers c
                 left join current cu on c.id = cu.customer_id
                 left join cumulative cum on c.id = cum.customer_id
                 left join adjustment a on c.id = a.client_id
        group by c.id, c.name;


END;
$$;

alter function get_monthly_report_by_year_and_month(integer, integer, text) owner to postgres;

