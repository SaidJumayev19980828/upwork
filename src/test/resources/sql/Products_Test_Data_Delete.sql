----------------------------deleting previous data----------------------------
delete from public.stocks where id between 601 and 604;
delete from public.products where id between 1001 and 1008;
delete from public.categories where id between 201 and 202;
delete from public.shops where id between 501 and 502;
delete from public.products where organization_id between 99000 and 99999;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 99000 and 99999;