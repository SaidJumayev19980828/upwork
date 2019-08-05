----------------------------deleting previous data----------------------------
delete from public.shops where id between 501 and 506;
delete from public.malls where id = 901;
delete from public.role_employee_users; --where id between 20 and 21;
delete from public.users where id in(88,89,90);
delete from public.employee_users where id in(68, 69,70 ,71, 158);
delete from public.roles; --where id between 1 and 3;
delete from public.brands where id between 101 and 103;
delete from public.organizations where id between 801 and 803;
----------------------------inserting dummy data----------------------------
--inserting Malls
INSERT INTO public.malls(id, name,created_at, updated_at) VALUES(901, 'mall_1', now(), now());

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (801, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (802, 'organization_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 802);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 801);

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 802);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 801);

--inserting users
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com',now(), now(), 'user1','123', 801);


INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, now(), now(), 'testuser1@nasnav.com', 801, '101112',  502);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 802, '131415',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (70, now(), now(), 'testuser4@nasnav.com', 801, '161718',  502);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (71, now(), now(), 'testuser5@nasnav.com', 801, '192021',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (158, now(), now(), 'testuser3@nasnav.com', 802, '222324',  501);

--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_MANAGER', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_MANAGER', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'STORE_EMPLOYEE', now(), now(), 801);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 4, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (23, 71, 5, now(), now());