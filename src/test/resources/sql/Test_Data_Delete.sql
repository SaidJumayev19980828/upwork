-----deleting previous data-----
delete from public.extra_attributes where id between 701 and 702;
delete from public.stocks where id between 601 and 604;
delete from public.products where id between 1001 and 1008;
delete from public.categories where id between 201 and 202;
delete from public.role_employee_users; --where id between 20 and 21;
delete from public.employee_users where id in(68, 69, 158);
delete from public.roles; --where id between 1 and 3;
delete from public.orders where id between 32 and 48;
delete from public.users where id in(88,89);
delete from public.shops where id between 501 and 502;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 801 and 802;