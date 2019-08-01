----------------------------deleting previous data----------------------------
delete from public.orders where id between 32 and 48;
delete from public.shops where id between 501 and 506;
delete from public.role_employee_users; --where id between 20 and 21;
delete from public.users where id in(88,89,90);
delete from public.employee_users where id in(68, 69,70 ,71, 158);
delete from public.roles; --where id between 1 and 3;
delete from public.brands where id between 101 and 103;
delete from public.organizations where id between 801 and 803;