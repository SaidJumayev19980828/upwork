SELECT  SETVAL(c.oid, 10000000)
from pg_class c JOIN pg_namespace n 
on n.oid = c.relnamespace 
where c.relkind = 'S' and n.nspname = 'public';