----------------------------deleting previous data----------------------------
delete from public.role_employee_users WHERE employee_user_id IN (SELECT id FROM employee_users where organization_id between 99000 and 99999);
delete from public.users where organization_id between 99000 and 99999;
delete from public.employee_users where organization_id between 99000 and 99999;
delete from public.roles; 
DELETE FROM public.files  where organization_id between 99000 and 99999;
DELETE FROM public.files  where orig_filename = 'nasnav--Test_Photo.png';  -- for test files with no organization
delete from public.organizations where id between 99000 and 99999;
