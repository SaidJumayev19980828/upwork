----------------------------deleting previous data----------------------------
delete from public.orders where id between 32 and 48;
delete from public.users where id in(88,89);
delete from public.shops where id between 501 and 502;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 99000 and 99999;