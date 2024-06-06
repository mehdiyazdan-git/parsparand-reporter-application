create or replace function get_persian_year(persian_date character varying) returns smallint
    language plpgsql
as
$$
DECLARE
    year_part character varying(4);
BEGIN
    year_part
        := substring(persian_date from 1 for 4);
    RETURN CAST(year_part AS smallint);
END;
$$;

alter function get_persian_year(varchar) owner to postgres;