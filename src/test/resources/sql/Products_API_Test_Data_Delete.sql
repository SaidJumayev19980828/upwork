----------------------------deleting previous data----------------------------
delete from public.role_employee_users where employee_user_id IN (SELECT id FROM public.employee_users where organization_id between 99000 and 99999);
delete from public.users where organization_id between 99000 and 99999;
delete from public.employee_users where organization_id between 99000 and 99999;
DELETE FROM public.roles WHERE organization_id BETWEEN 99000 AND 99999;
delete from public.stocks where organization_id between 99000 and 99999;
delete from public.products where organization_id between 99000 and 99999;
delete from public.categories where id between 201 and 202;
delete from public.shops where organization_id between 99000 and 99999;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 99000 and 99999;