create function get_persian_month(persian_date character varying) returns smallint
    language plpgsql
as
$$
DECLARE
    month_part character varying(2);
BEGIN
    month_part
        := substring(persian_date from 6 for 2);
    RETURN CAST(month_part AS smallint);
END;
$$;

alter function get_persian_month(varchar) owner to postgres;

