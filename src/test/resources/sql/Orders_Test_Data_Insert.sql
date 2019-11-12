
----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99003, 'organization_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (103, 202, 'brand_3', now(), now(), 99001);

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'category_1', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'category_2', now(), now());

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (503, 'shop_3', 102, now(), now(), 99001);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (504, 'shop_4', 103, now(), now(), 99003);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (505, 'shop_5', 101, now(), now(), 99003);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (506, 'shop_6', 102, now(), now(), 99002);

--inserting users
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com',now(), now(), 'user1','123', 99001);
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com',now(), now(), 'user2','456', 99002);
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com',now(), now(), 'user3','789', 99003);


INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (70, now(), now(), 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (71, now(), now(), 'testuser5@nasnav.com', 99003, '192021',  504);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (158, now(), now(), 'testuser3@nasnav.com', 99002, '222324',  506);

--inserting Roles
insert into public.roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 4, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (23, 71, 5, now(), now());

--inserting orders
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(33, 88, now(), now(), 99001, 0, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(37, 88, now(), now(), 99002, 0, 501);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(38, 90, now(), now(), 99002, 1, 501);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(39, 88, now(), now(), 99001, 1, 501);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(40, 88, now(), now(), 99001, 0, 503);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(41, 90, now(), now(), 99001, 1, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(42, 88, now(), now(), 99001, 1, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(43, 88, now(), now(), 99002, 0, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(34, 89, now(), now(), 99003, 0, 504);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(35, 89, now(), now(), 99002, 0, 501);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(36, 90, now(), now(), 99003, 1, 505);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(44, 89, now(), now(), 99003, 1, 505);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(45, 89, now(), now(), 99001, 0, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(46, 90, now(), now(), 99002, 1, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(47, 89, now(), now(), 99001, 1, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(48, 89, now(), now(), 99002, 0, 502);



--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 1001);


-- inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(601, 502, 0, now(), now(), 99001, 0.0, 310001);
