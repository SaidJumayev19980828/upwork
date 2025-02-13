
----------------------------inserting dummy data----------------------------
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');
INSERT INTO public.organizations(id, name) VALUES (99003, 'organization_2');

--insert organization domain
INSERT INTO public.organization_domains
("domain", organization_id, subdir)
VALUES('develop.nasnav.org', 99001, null);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 202, 'brand_3', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

-- insert addresses
INSERT INTO public.addresses(id, address_line_1) values(12300001, 'address line');

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (501, 'shop_1', 102, 99001, 0, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (502, 'shop_2', 101, 99001, 0, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (503, 'shop_3', 102, 99001, 0, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (504, 'shop_4', 103, 99001, 0, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (505, 'shop_5', 101, 99001, 0, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (506, 'shop_6', 102, 99002, 0, 12300001);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'ahmed.galal@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (90, 'user3@nasnav.com','user3','789', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);


-- insert user addresses
INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);


--insert employee users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'test2@nasnav.com', 99001, '131415',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (158, 'test4@nasnav.com', 99002, '222324',  506);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (103, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (104, '131415', now(), 69, null);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (109, '222324', now(), 158, null);
--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);
insert into public.roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(7, 'ORGANIZATION_MANAGER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 7);




--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',101, 201, 99002, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1003);
insert into public.product_variants(id, "name" , product_id ) values(310004, 'var' 	, 1004);
insert into public.product_variants(id, "name" , product_id ) values(310005, 'var' 	, 1004);
insert into public.product_variants(id, "name" , product_id ) values(310006, 'var' 	, 1005);


-- inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 20, 99001, 60.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 10, 99001, 70.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 502, 5, 99001, 0.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 503, 5, 99001, 0.0, 310004);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(605, 505, 5, 99001, 0.0, 310005);

--insert shipping service
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters, id)VALUES('TEST', 99001, '{"hotline":"19888"}', 11001);
--inserting orders
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status, grand_total) VALUES(310001 , now(),88, 99001, 1, 130.0);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310002 , now(),89, 99002, 1);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310003 , now() - INTERVAL '100 DAY' ,88, 99001, 1);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310004 , now(),88, 99001, 1);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id )
values(330031, 88, now(), now(), 99001, 1, 501, 310001, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330032, 88, now(), now(), 99001, 1, 502, 310001, 12300001);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id )
values(330033, 89, now(), now(), 99002, 1, 506, 310002, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330034, 89, now(), now(), 99002, 1, 506, 310002, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330035, 88, now() - INTERVAL '100 DAY', now() - INTERVAL '100 DAY', 99001, 1, 502, 310003, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330036, 88, now(), now(), 99001, 1, 502, 310004, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330037, 88, now(), now(), 99001, 1, 505, 310004, 12300001);

INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status)
VALUES(330031, 'TEST', '{"Shop Id":501}' , now(), now(), 0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status)
VALUES(330032, 'TEST', '{"Shop Id":502}' , now(), now(), 0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status)
VALUES(330036, 'TEST', '{"Shop Id":505}' , now(), now(), 0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status)
VALUES(330037, 'TEST', '{"Shop Id":505}' , now(), now(), 0);


-- insert order items
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330031, 330031, 601, 14, 60.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330032, 330032, 605, 2, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330035, 330035, 604, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330036, 330036, 604, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330037, 330037, 601, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330038, 330036, 602, 3, 70.0, 1);


