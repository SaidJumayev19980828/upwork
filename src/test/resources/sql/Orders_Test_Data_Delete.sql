----------------------------deleting previous data----------------------------
delete from public.orders where id between 32 and 48;
delete from public.shops where id between 501 and 506;
delete from public.role_employee_users; --where id between 20 and 21;
delete from public.users where organization_id between 99000 and 99999;
delete from public.employee_users where organization_id between 99000 and 99999;
delete from public.roles; --where id between 1 and 3;
delete from public.brands where organization_id between 99000 and 99999;
delete from public.organizations where id between 99000 and 99999;
