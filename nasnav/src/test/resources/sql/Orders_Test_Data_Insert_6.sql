
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


--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 202, 'brand_3', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');


--register the organization to dummy shipping service
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters, id) VALUES('TEST', 99001, '{"Hot Line": 911, "Shops":["501"]}', 11001);




INSERT INTO public.addresses(id, address_line_1) values(12300002, 'Ali papa cave');


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (501, 'shop_1', 102, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (502, 'shop_2', 101, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (503, 'shop_3', 102, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (504, 'shop_4', 103, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (505, 'shop_5', 101, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (506, 'shop_6', 102, 99002, 12300002);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, user_status)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001, 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, user_status)
    VALUES (89, 'user2@nasnav.com','user2','456', 99002, 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, user_status)
    VALUES (90, 'user3@nasnav.com','user3','789', 99003, 201);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null, 89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (110, '666', now(), null, 90);

-- insert user addresses


INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300001, 'address line', 1, '01111234567', 888001);

INSERT INTO public.User_addresses(id,user_id, address_id, principal) values(12300001, 88, 12300001, false);


-- insert employee addresses
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  504);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (72, 'testuser6@nasnav.com', 99001, 'sdrf8s',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (73, 'testuser7@nasnav.com', 99001, 'sdfe47',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99001, '222324',  506);

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




--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, removed) VALUES (1001, 'product_1',101, 201, 99001, now(), now(), 1);
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, removed) VALUES (1002, 'product_2',101, 201, 99001, now(), now(), 1);
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',101, 201, 99001, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id, removed ) values(310001, 'var' 	, 1001, 1);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1003);
insert into public.product_variants(id, "name" , product_id ) values(310004, 'var' 	, 1004);


-- inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 20, 99001, 60.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 10, 99001, 70.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 502, 2, 99001, 0.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 503, 0, 99001, 0.0, 310004);


--inserting orders
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310001 , now(),88, 99001, 8);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id ) 
values(330031, 88, now(), now(), 99001, 8, 501, 310001, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id ) 
values(330032, 88, now(), now(), 99002, 2, 502, 310001, 12300001);

INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330031, 'TEST', '{"Shop Id":501}' , now(), now(), 0, 20.0);
INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(330032, 'TEST', '{"Shop Id":502}' , now(), now(), 0, 12.0);


-- insert order items
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330031, 601, 14, 60.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330032, 602, 2, 70.0, 1);


INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(330031, '', 'ssss', 2, now(), 980, 2, 'dfdfdd', 88, 310001);




-- insert cart
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(602, '99001/img2.jpg', '{"Color":"Blue"}', 2, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(603, '99001/cover_img.jpg', '{"Color":"Yellow"}', 4, 88);




--INSERT dummy images
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img1.jpg', '/dummy_loc1.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img2.jpg', '/dummy_loc2.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img3.jpg', '/dummy_loc3.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img.jpg', '/dummy_loc4.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img2.jpg', '/dummy_loc5.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img3.jpg', '/dummy_loc6.jpg', 'image/jpeg', 'dummy_loc.jpg');


INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, 310003, 7, 0, '99001/img1.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, 310003, 7, 1, '99001/img2.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, null, 7, 0, '99001/img3.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1002, 310002, 7, 1, '99001/cover_img.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1002, null, 7, 1, '99001/cover_img2.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1002, null, 7, 0, '99001/cover_img3.jpg');

