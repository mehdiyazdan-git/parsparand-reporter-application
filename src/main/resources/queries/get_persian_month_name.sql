create or replace function get_persian_month_name(persian_month integer) returns text
    language plpgsql
as
$$
BEGIN
    RETURN CASE persian_month
               WHEN 1 THEN 'فروردین'
               WHEN 2 THEN 'اردیبهشت'
               WHEN 3 THEN 'خرداد'
               WHEN 4 THEN 'تیر'
               WHEN 5 THEN 'مرداد'
               WHEN 6 THEN 'شهریور'
               WHEN 7 THEN 'مهر'
               WHEN 8 THEN 'آبان'
               WHEN 9 THEN 'آذر'
               WHEN 10 THEN 'دی'
               WHEN 11 THEN 'بهمن'
               WHEN 12 THEN 'اسفند'
               ELSE ''
        END;
END;
$$;

alter function get_persian_month_name(integer) owner to postgres;

