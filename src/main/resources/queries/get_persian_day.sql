create function get_persian_day(persian_date character varying) returns smallint
    language plpgsql
as
$$
DECLARE
    day_part character varying(2);
BEGIN
    day_part
        := substring(persian_date from 9 for 2);
    RETURN CAST(day_part AS smallint);
END;
$$;

alter function get_persian_day(varchar) owner to postgres;