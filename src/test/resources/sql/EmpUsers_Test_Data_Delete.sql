----------------------------deleting previous data----------------------------
delete from public.role_employee_users; --where id between 20 and 21;
delete from public.employee_users where id in(68, 69, 158);
delete from public.roles; --where id between 1 and 3;
delete from public.shops where id between 501 and 502;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 801 and 802;