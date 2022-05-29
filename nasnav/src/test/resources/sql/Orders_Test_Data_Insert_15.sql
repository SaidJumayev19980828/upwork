
----------------------------inserting dummy data----------------------------
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);
INSERT INTO public.organizations(id, name) VALUES (99003, 'organization_2');

-- insert countries, cities, areas and sub-areas
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888001, 1, 'Badr city', 99001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888002, 1, 'Badr city', 99002);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com','user3','789', 99003);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, user_status)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112', 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, user_status)
	VALUES (69, 'testuser2@nasnav.com', 99002, '131415', 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, user_status)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718', 201);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111103, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111104, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111105, '161718', now(), 70, null);

--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);
insert into public.roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(7, 'ORGANIZATION_MANAGER', 99001);

INSERT INTO public.addresses(id, address_line_1,area_id,sub_area_id) values(12300001, 'address line',1 , 888002);
INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 69, 7);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 70, 4);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 202, 'brand_3', 99002);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_3', 103, 99002, 0);

--inserting products
INSERT INTO public.products(id, name, brand_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1', 101, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2', 102, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3', 103, 99002, now(), now());

-- variants for each product
insert into public.product_variants(id, name, product_id) values(310001, 'var', 1001);
insert into public.product_variants(id, name, product_id) values(310002, 'var', 1002);
insert into public.product_variants(id, name, product_id) values(310003, 'var', 1003);
insert into public.product_variants(id, name, product_id) values(310004, 'var', 1003);

--inserting meta orders
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310031 , now(),88, 99001, 2);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310032 , now(),89, 99001, 8);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310033 , now(),90, 99001, 8);

INSERT INTO public.payments
(id, operator, uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(210031, 'COD', 'COD', 2, now(), 980, 2, 'dfdfdd', 88, 310031);
INSERT INTO public.payments
(id, operator, uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(210032, 'MCARD:CIB:RASPORT', 'MCARD:CIB:RASPORT', 2, now(), 980, 2, 'dfdfdd', 89, 310032);
INSERT INTO public.payments
(id, operator, uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(210033, 'COD', 'COD', 2, now(), 980, 2, 'dfdfdd', 90, 310033);

insert into public.orders(id,name, user_id,created_at, updated_at, organization_id, status,shop_id, meta_order_id, payment_id, total) values(330031, 'name_1', 88, now() - INTERVAL '7 DAY', now(), 99001, 2, 501, 310031, 210031, 900.00);
insert into public.orders(id,name, user_id,created_at, updated_at, organization_id, status,shop_id, meta_order_id, payment_id, total) values(330032, 'name_1', 88, now() - INTERVAL '5 DAY', now(), 99001, 5, 502, 310032, 210031, 500.00);
insert into public.orders(id,name, user_id,created_at, updated_at, organization_id, status,shop_id, meta_order_id, payment_id, total) values(330033, 'name_2', 89, now(), now(), 99002, 1, 503, 310032, 210033, 100.00);
insert into public.orders(id,name, user_id,created_at, updated_at, organization_id, status,shop_id, meta_order_id, payment_id, total) values(330034, 'name_3', 90, now(), now(), 99002, 3, 503, 310033, 210033, 2000.00);

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 10, 99001, 50.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 10, 99001, 40.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 503, 10, 99002, 80.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 503, 10, 99002, 10.0, 310004);

INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330031, 601, 5, 50, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330031, 601, 10, 50, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330032, 602, 3, 40, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330032, 602, 2, 80, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330033, 603, 2, 10, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330034, 604, 7, 100, 1);

INSERT INTO public.shipment
(sub_order_id, shipping_service_id, created_at, updated_at, status, shipping_fee)
VALUES(330031, 'BOSTA_LIVES',now(), now(), 0, 20.0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, created_at, updated_at, status, shipping_fee)
VALUES(330032, 'BOSTA_LIVES', now(), now(), 0, 12.0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, created_at, updated_at, status, shipping_fee)
VALUES(330033, 'BOSTA_LIVES', now(), now(), 0, 20.0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, created_at, updated_at, status, shipping_fee)
VALUES(330034, 'BOSTA_LIVES', now(), now(), 0, 12.0);