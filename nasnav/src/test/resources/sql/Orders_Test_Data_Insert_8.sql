
----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);


--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);
INSERT INTO public.organizations(id, name) VALUES (99003, 'organization_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 202, 'brand_3', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');


--register the organization to dummy shipping service
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters, id) VALUES('TEST', 99001, '{"Hot Line": 911, "Shops":["501"]}', 11001);




INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300002, 'Ali papa cave', 1, '+1111234567');


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (501, 'shop_1', 102, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (502, 'shop_2', 101, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (503, 'shop_3', 102, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (504, 'shop_4', 103, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (505, 'shop_5', 101, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (506, 'shop_6', 102, 99002, 12300002);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com','user3','789', 99003);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);

-- insert user addresses
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300001, 'address line', 1, '01111234567');

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);


-- insert employee addresses
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  504, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (72, 'testuser6@nasnav.com', 99001, 'sdrf8s',  501, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (73, 'testuser7@nasnav.com', 99001, 'sdfe47',  502, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (158, 'testuser3@nasnav.com', 99001, '222324',  506, 201);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (103, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (104, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (105, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (106, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (107, 'sdrf8s', now(), 72, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id ,user_id) VALUES (108, 'sdfe47', now(), 73, null);
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
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (25, 71, 5);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (26, 72, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (27, 73, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (28, 69, 7);



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
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 20, 99001, 60.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 10, 99001, 70.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 502, 2, 99001, 0.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 503, 0, 99001, 0.0, 310004);


--inserting orders
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status, referral_withdraw_amount) VALUES(310001 , now(),88, 99001, 8, 50);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status, grand_total) VALUES(310002 , now(),88, 99001, 1, 1000.00);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310003 , now(),88, 99001, 8);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id ) 
values(330031, 88, now(), now(), 99001, 8, 501, 310001, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id ) 
values(330032, 88, now(), now(), 99001, 8, 502, 310001, 12300001);


insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id ) 
values(330033, 88, now(), now(), 99001, 1, 501, 310002, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id )
values(330034, 88, now(), now(), 99001, 1, 502, 310002, 12300001);


insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id ) 
values(330035, 88, now(), now(), 99001, 8, 501, 310003, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id ) 
values(330036, 88, now(), now(), 99001, 2, 502, 310003, 12300001);



INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330031, 'TEST', '{"Shop Id":501}' , now(), now(), 0, 20.0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330032, 'TEST', '{"Shop Id":502}' , now(), now(), 0, 12.0);

INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330033, 'TEST', '{"Shop Id":501}' , now(), now(), 0, 20.0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330034, 'TEST', '{"Shop Id":502}' , now(), now(), 0, 12.0);



INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330035, 'TEST', '{"Shop Id":501}' , now(), now(), 0, 20.0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330036, 'TEST', '{"Shop Id":502}' , now(), now(), 0, 12.0);


-- insert order items
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330031, 601, 14, 60.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330032, 602, 2, 70.0, 1);

INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330033, 601, 14, 60.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330034, 602, 2, 70.0, 1);


-- insert cart
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(602, '99001/img2.jpg', '{"Color":"Blue"}', 2, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(603, '99001/cover_img.jpg', '{"Color":"Yellow"}', 4, 88);

INSERT INTO public.referral_wallet(id, balance, version, user_id, referral_type) VALUES (500, 20.00, 1, 88, 'USER');

