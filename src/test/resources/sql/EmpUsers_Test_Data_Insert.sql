----------------------------deleting previous data----------------------------
delete from public.role_employee_users; --where id between 20 and 21;
delete from public.employee_users where id in(68, 69,70 ,71, 158);
DELETE FROM PUBLIC.ORDERS WHERE organization_id BETWEEN 99000 AND 99999;
delete from public.roles; --where id between 1 and 3;
delete from public.shops where id between 501 and 502;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 99001 and 99002;


--///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--inserting Employee Users
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, 'abcdefg',  501, '#123');
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, 'hijkllm',  501, '#123');
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (70, now(), now(), 'testuser4@nasnav.com', 99001, '123',  501, '#123');
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (71, now(), now(), 'testuser5@nasnav.com', 99001, '456',  501, '#123');
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (158, now(), now(), 'testuser3@nasnav.com', 99001, 'nopqrst',  501, '#123');

--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 4, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (23, 71, 5, now(), now());