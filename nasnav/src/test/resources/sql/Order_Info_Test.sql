
----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);
INSERT INTO public.organizations(id, name) VALUES (99003, 'organization_2');

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');


--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 202, 'brand_3', 99001);



--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_3', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (504, 'shop_4', 101, 99003, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (506, 'shop_6', 101, 99002, 0);


--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com','user3','789', 99003);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
	VALUES (91, 'user4@nasnav.com','user4','011', 99003);--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88001, 'user1@nasnav.com','user1','88657aser', 99003);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '456', now(), null, 89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '789', now(), null, 90);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '011', now(), null, 91);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '88657aser', now(), null, 88001);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99003, '192021',  504);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  506);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (7, '161718', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (8, 'abcdefg', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (9, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (10, '222324', now(), 158, null);


--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);



--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 5);


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());



-- variants for each product
insert into public.product_variants(id, "name" , product_id, removed ) values(310001, 'var' 	, 1001, 1);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1001);



--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 6, 99002, 600.00, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 501, 8, 99001, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 501, 4, 99002, 200.00, 310003);




--INSERT dummy images
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img1.jpg', '/dummy_loc1.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img2.jpg', '/dummy_loc2.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img3.jpg', '/dummy_loc3.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img.jpg', '/dummy_loc4.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img2.jpg', '/dummy_loc5.jpg', 'image/jpeg', 'dummy_loc.jpg');


INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, 310001, 7, 1, '99001/img1.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, 310002, 7, 1, '99001/img2.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, null, 7, 1, '99001/img3.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, null, 7, 0, '99001/cover_img.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, null, 7, 0, '99001/cover_img2.jpg');


--inserting meta orders
INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status, referral_withdraw_amount) VALUES(310001 , now(),88, 99001, 8, 20.00);

INSERT INTO public.payments(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
    VALUES(null, 'S.C.A.M', 'ssss', 2, now(), 980, 2, 'dfdfdd', 88, 310001);
INSERT INTO public.payments(order_id, "operator", uid, status, executed, amount, currency, "object", user_id, meta_order_id)
    VALUES(null, 'S.C.A.M', 'ssss', 2, now(), 980, 2, 'dfdfddd', 88, 310001);

--inserting orders
INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330002, '', '', 88, '2022-02-01', now(), now(), 99001, 1, '{}'::character varying[], 502, '{}'::text, 600.00, 0, 600.00, 310001);


INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id, applied_referral_code)
VALUES(330003, '', '', 88, '2022-02-02', now() + interval '2 day', now(), 99001, 0, '{}'::character varying[], 502, '{}'::text, 300.00, 0, 300.00, 310001, 'abcdfg');


INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330004, '', '', 89, '2022-02-03', now() + interval '1 day', now(), 99002, 0, '{}'::character varying[], 502, '{}'::text, 200.00, 0, 200.00, 310001);

INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330005, '', '', 89, '2022-02-04', now() + interval '3 day', now(), 99002, 0, '{}'::character varying[], 502, '{}'::text, 50.00, 0, 50.00, 310001);


INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330006, '', '', 90, '2022-02-05', now() + interval '4 day', now(), 99003, 1, '{}'::character varying[], 502, '{}'::text, 100.00, 0, 100.00, 310001);



INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330002, 601, 14, 600.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330003, 601, 7, 300.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency,discount)VALUES(330004, 601, 5, 200.0, 1, 100);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330005, 601, 1, 50.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330006, 601, 3, 100.0, 1);



