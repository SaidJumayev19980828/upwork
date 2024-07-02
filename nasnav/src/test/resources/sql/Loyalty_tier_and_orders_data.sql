
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



--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 202, 'brand_3', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_3', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (504, 'shop_4', 103, 99003, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (505, 'shop_5', 101, 99003, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (506, 'shop_6', 102, 99002, 0);


--insert loyalty tiers and configurations
INSERT INTO public.loyalty_tier(id, tier_name, is_active, created_at, no_of_purchase_from, no_of_purchase_to, organization_id, constraints) VALUES (1, 'First_tier', true, now(), 1, 5, 99001, '{"ORDER_ONLINE":0.05}');
INSERT INTO public.loyalty_tier(id, tier_name, is_active, created_at, no_of_purchase_from, no_of_purchase_to, organization_id, constraints) VALUES (2, 'Second_tier', true, now(), 6, 20, 99001, '{"ORDER_ONLINE":0.05}');

insert into public.loyalty_point_config
values (31001, 'description', 99001, 501, true, now(), 1, '{"ORDER_ONLINE":{"ratio_from":7, "ratio_to":1}}');
insert into public.loyalty_point_config
values (31002, 'description_2', 99001, 501, true, now(), 2, '{"ORDER_ONLINE":{"ratio_from":7, "ratio_to":1}}');

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id,tier_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001,1);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (90, 'user3@nasnav.com','user3','789', 99003);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (71, 'testuser5@nasnav.com', 99003, '192021',  504, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (72, 'testuser6@nasnav.com', 99003, 'sdrf8s',  504, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (73, 'testuser7@nasnav.com', 99003, 'sdfe47',  505, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (74, 'testuser8@nasnav.com', 99001, '252627',  502, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  506, 201);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111103, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111104, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111105, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111106, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111107, 'sdrf8s', now(), 72, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111108, 'sdfe47', now(), 73, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111109, '252627', now(), 74, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (111110, '222324', now(), 158, null);

--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);
insert into public.roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(7, 'ORGANIZATION_MANAGER', 99001);


INSERT INTO public.addresses(id, address_line_1,area_id,sub_area_id) values(12300001, 'address line',1 , 888002);
INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',101, 201, 99001, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1003);
insert into public.product_variants(id, "name" , product_id ) values(310004, 'var' 	, 1004);


-- inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 0, 99001, 0.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 0, 99001, 0.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 503, 0, 99001, 0.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 503, 0, 99001, 0.0, 310004);


--inserting meta orders
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310033 , now(),88, 99001, 2);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310037 , now(),88, 99001, 8);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310038 , now(),88, 99001, 8);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310039 , now(),88, 99001, 8);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310040 , now(),88, 99001, 8);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310041 , now(),88, 99001, 8);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310042 , now(),88, 99001, 8);

--inserting orders
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id) values(330033, 88, now(), now(), 99001, 2, 502, 310033);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id) values(330037, 88, now(), now(), 99001, 8, 502, 310033);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id) values(330038, 90, now(), now(), 99002, 1, 501, 310038, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id) values(330039, 88, now(), now(), 99001, 8, 501, 310039);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id) values(330040, 88, now(), now(), 99001, 8, 501, 310040);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id) values(330041, 88, now(), now(), 99001, 8, 501, 310041);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id) values(330042, 88, now(), now(), 99001, 8, 501, 310042);

-- insert order items
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330033, 601, 14, 600.0, 1);
