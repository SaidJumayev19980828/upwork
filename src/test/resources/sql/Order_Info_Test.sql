
----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99003, 'organization_2', now(), now());


--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (103, 202, 'brand_3', now(), now(), 99001);



--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);


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
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (70, now(), now(), 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (71, now(), now(), 'testuser5@nasnav.com', 99003, '192021',  504);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (158, now(), now(), 'testuser3@nasnav.com', 99002, '222324',  506);



--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);



--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 4, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (23, 71, 5, now(), now());


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());



-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1001);



--inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(601, 502, 6, now(), now(), 99002, 600.00, 310001);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(602, 501, 8, now(), now(), 99001, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(603, 501, 4, now(), now(), 99002, 200.00, 310003);




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




--inserting orders
INSERT INTO public.orders
(id,address, email, "name", payment_type, user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, driver_name, equipped, basket, amount, payment_status)
VALUES(330002,'', '', '', 0, 88, now(), now(), now(), 99001, 1, '{}'::character varying[], 502, 'zeko', false, '{}'::text, 600.00, 0);


INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330002, 601, 14, 600.0, 1);


