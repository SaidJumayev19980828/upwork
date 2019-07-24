----------------------------deleting previous data----------------------------
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

----------------------------inserting dummy data----------------------------

----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (701, 'size', 'boolean', 802, '/uploads/category/fearutes/feature1.jpg', now(), now());
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (702, 'filter', 'boolean', 801, '/uploads/category/logo/logo1.jpg', now(), now());

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (801, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (802, 'organization_2', now(), now());

--inserting Employee Users
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, now(), now(), 'testuser1@nasnav.com', 801, 'abcdefg',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 801, 'hijkllm',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (158, now(), now(), 'testuser3@nasnav.com', 801, 'nopqrst',  501);

--inserting users
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com',now(), now(), 'user1','abdcefg', 801);
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com',now(), now(), 'user2','hijklm', 802);

--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 801);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 802);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 801);

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'category_1', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'category_2', now(), now());

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 802);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 801);

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 801, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',101, 201, 802, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 801, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 801, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 202, 801, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 802, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 802, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 802, now(), now());

--inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(601, 502, 6, now(), now(), 802, 600.0, 1001);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(602, 501, 8, now(), now(), 801, 1200.0, 1002);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(603, 501, 4, now(), now(), 802, 200.0, 1003);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(604, 502, 6, now(), now(), 801, 700.0, 1004);

--inserting orders
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(33, 88, now(), now(), 801, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(37, 88, now(), now(), 802, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(38, 88, now(), now(), 801, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(39, 88, now(), now(), 802, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(40, 88, now(), now(), 801, 0, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(41, 88, now(), now(), 802, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(42, 88, now(), now(), 801, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(43, 88, now(), now(), 802, 0, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(34, 89, now(), now(), 801, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(35, 89, now(), now(), 802, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(36, 89, now(), now(), 801, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(44, 89, now(), now(), 802, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(45, 89, now(), now(), 801, 0, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(46, 89, now(), now(), 802, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(47, 89, now(), now(), 801, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(48, 89, now(), now(), 802, 0, 502);


