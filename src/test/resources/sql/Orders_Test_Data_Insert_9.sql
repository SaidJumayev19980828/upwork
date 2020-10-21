
----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
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

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_3', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (504, 'shop_4', 103, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (505, 'shop_5', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (506, 'shop_6', 102, 99002, 0);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (90, 'user3@nasnav.com','user3','789', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);


-- insert user addresses
INSERT INTO public.addresses(id, address_line_1) values(12300001, 'address line');

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);


--insert employee users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser5@nasnav.com', 99003, '192021',  504);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (72, 'testuser6@nasnav.com', 99001, 'sdrf8s',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (73, 'testuser7@nasnav.com', 99001, 'sdfe47',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  506);

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
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 69, 7);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (25, 71, 5);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (26, 72, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (27, 73, 6);




--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',101, 201, 99001, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id, product_code, sku ) values(310001, 'var' 	, 1001, 'ABC', '123');
insert into public.product_variants(id, "name" , product_id, product_code, sku ) values(310002, 'var' 	, 1002, 'ETR', '456');
insert into public.product_variants(id, "name" , product_id, product_code, sku ) values(310003, 'var' 	, 1003, 'SDF', '789');
insert into public.product_variants(id, "name" , product_id, product_code, sku ) values(310004, 'var' 	, 1004, 'WER', '735');


-- inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 20, 99001, 60.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 10, 99001, 70.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 502, 5, 99001, 0.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 503, 5, 99001, 0.0, 310004);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(605, 506, 5, 99002, 0.0, 310004);


--inserting orders
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status, grand_total) VALUES(310001 , now(),88, 99001, 8, 130.0);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310002 , now(),89, 99001, 5);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310003 , now() - INTERVAL '100 DAY' ,88, 99001, 5);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310004 , now(),88, 99001, 5);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310005 , now(),88, 99002, 5);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310006 , now(),88, 99001, 5);
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310007 , now(),88, 99001, 1);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id )
values(330031, 88, now(), now(), 99001, 5, 501, 310001, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330032, 88, now(), now(), 99001, 5, 502, 310001, 12300001);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id )
values(330033, 89, now(), now(), 99001, 5, 501, 310002, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330034, 89, now(), now(), 99001, 5, 502, 310002, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330035, 88, now() - INTERVAL '100 DAY', now() - INTERVAL '100 DAY', 99001, 1, 502, 310003, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330036, 88, now(), now(), 99001, 5, 502, 310004, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330037, 88, now(), now(), 99002, 5, 506, 310005, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330038, 88, now(), now(), 99001, 5, 502, 310006, 12300001);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id)
values(330039, 88, now(), now(), 99001, 1, 502, 310007, 12300001);

INSERT INTO public.shipment
(id, sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, track_number, external_id)
VALUES(330031, 330031, 'TEST', '{"Shop Id":501}' , now(), now(), 0, 'abc44556', '88663');
INSERT INTO public.shipment
(id, sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, track_number, external_id)
VALUES(330032, 330032, 'TEST', '{"Shop Id":502}' , now(), now(), 0, 'xyz13245', '1234');


--insert payments
INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(null, 'COD', 'wqww', 2, now(), 3151, 2, 'dfdfdd', 88, 310001);
INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(null, 'COD', 'bbbb', 2, now(), 3151, 2, 'erewr', 88, 310002);
INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(null, 'COD', 'sssfffs', 2, now(), 3151, 2, 'aveafd', 88, 310003);
INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(null, 'COD', 'ttt', 2, now(), 3151, 2, 'zdxf', 88, 310004);
INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(null, 'COD', 'ccc', 2, now(), 3151, 2, 'vsasdsf', 88, 310005);
INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(null, 'COD', 'zzz', 2, now(), 3151, 2, 'zsvzxf', 88, 310006);
INSERT INTO public.payments
(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
VALUES(null, 'COD', 'www', 2, now(), 3151, 2, 'qwe33', 88, 310007);



-- insert order items
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330031, 330031, 601, 14, 60.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330032, 330032, 602, 2, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330033, 330033, 603, 3, 60.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330034, 330034, 604, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330035, 330035, 604, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330036, 330036, 604, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330037, 330036, 601, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330038, 330036, 602, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330039, 330037, 605, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330040, 330038, 604, 3, 70.0, 1);
INSERT INTO public.baskets(id, order_id, stock_id, quantity, price, currency)VALUES(330041, 330039, 604, 3, 70.0, 1);




--insert return shipments
INSERT INTO public.return_shipment(id, created_at, updated_at, external_id, shipping_service_id, status, track_number)
values(360001, now(), now(), 'reafd', 'TEST', 1, '1234');
INSERT INTO public.return_shipment(id, created_at, updated_at, external_id, shipping_service_id, status, track_number)
values(360002, now(), now(), 'cxvzv', 'TEST', 1, '56578');
INSERT INTO public.return_shipment(id, created_at, updated_at, external_id, shipping_service_id, status, track_number)
values(360003, now(), now(), 'qeqqwe', 'TEST', 1, '234213');
INSERT INTO public.return_shipment(id, created_at, updated_at, external_id, shipping_service_id, status, track_number)
values(360004, now(), now(), 'vxfgg', 'TEST', 1, '565464');
INSERT INTO public.return_shipment(id, created_at, updated_at, external_id, shipping_service_id, status, track_number)
values(360005, now(), now(), 'ufyfu', 'TEST', 1, '1234423');
INSERT INTO public.return_shipment(id, created_at, updated_at, external_id, shipping_service_id, status, track_number)
values(360006, now(), now(), 'uio', 'TEST', 1, '15435');

-- insert return request
INSERT INTO public.return_request
(id , created_on, created_by_user, created_by_employee, meta_order_id, status)
VALUES(440034, now(), 88, null, 310002, 1);

INSERT INTO public.return_request_item
(return_request_id, order_item_id, returned_quantity, received_quantity, received_by, received_on, created_by_user, created_by_employee)
VALUES(440034, 330036, 1, 1, 69, now(), 88, null);




-- insert cart
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(602, '99001/img2.jpg', '{"Color":"Blue"}', 2, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(603, '99001/cover_img.jpg', '{"Color":"Yellow"}', 4, 88);

INSERT INTO public.return_request(id, created_on, created_by_employee, meta_order_id, status)
    VALUES(330031, now(), 69, 310001, 0);
INSERT INTO public.return_request(id, created_on, created_by_employee, meta_order_id, status)
    VALUES(330032, now() - interval '100 DAY', 69, 310002, 3);
INSERT INTO public.return_request(id, created_on, created_by_employee, meta_order_id, status)
    VALUES(330033, now() , 69, 310005, 0);
INSERT INTO public.return_request(id, created_on, created_by_employee, meta_order_id, status)
    VALUES(330036, now() , 69, 310006, 0);

INSERT INTO public.return_request_item(id, return_request_id, order_item_id, returned_quantity, received_on, return_shipment_id)
    VALUES(330031, 330031, 330031, 1, null, 360001);
INSERT INTO public.return_request_item(id, return_request_id, order_item_id, returned_quantity, received_on, return_shipment_id)
    VALUES(330032, 330031, 330032, 1, null, 360001);
INSERT INTO public.return_request_item(id, return_request_id, order_item_id, returned_quantity, received_on, return_shipment_id)
    VALUES(330033, 330032, 330033, 1, null, 360002);
INSERT INTO public.return_request_item(id, return_request_id, order_item_id, returned_quantity, received_on, return_shipment_id)
    VALUES(330034, 330033, 330039, 1, null, 360003);
INSERT INTO public.return_request_item(id, return_request_id, order_item_id, returned_quantity, received_on, return_shipment_id)
    VALUES(330035, 330036, 330040, 1, now(), 360006);