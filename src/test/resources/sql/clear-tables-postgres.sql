CREATE OR REPLACE FUNCTION public.truncate_tables(_username text)
  RETURNS void AS
$func$
BEGIN
    EXECUTE  
  (SELECT 'TRUNCATE TABLE '
       || string_agg(quote_ident(schemaname) || '.' || quote_ident(tablename), ', ')
       || ' CASCADE'
   FROM   pg_tables
   WHERE  tableowner = _username
   AND    schemaname = 'public'
   );
END
$func$ LANGUAGE plpgsql;


SELECT truncate_tables( (select user));

-- creates an executes a function that truncates all the tables before running tests
-- this provides more deterministic test results