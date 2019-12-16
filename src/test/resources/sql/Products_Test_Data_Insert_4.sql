----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'category_1', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'category_2', now(), now());

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--insering users
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (69, now(), now(), 'testuser2@nasnav.com', 99002, '131415',  501);


--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());



--inserting products
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES (1001, 'product_1', 'product-one',101, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, barcode, cover_image) VALUES (1002, 'product_2', 'product-two',101, 201, 99002, now(), now(),'123456789', 'my_cool_img.jpg');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES (1003, 'product_3', 'product-three',101, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES (1004, 'product_4', 'product-four',102, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES (1005, 'product_5', 'product-five',102, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES (1006, 'product_6', 'product-six',102, 201, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES (1007, 'product_7', 'product-seven',101, 202, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES (1008, 'product_8', 'product-eight',102, 202, 99002, now(), now(), 'my_cool_img.jpg');



-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1003);
insert into public.product_variants(id, "name" , product_id ) values(310005, 'var' 	, 1005);
insert into public.product_variants(id, "name" , product_id ) values(310006, 'var' 	, 1006);
insert into public.product_variants(id, "name" , product_id ) values(310007, 'var' 	, 1007);
insert into public.product_variants(id, "name" , product_id ) values(310008, 'var' 	, 1008);

-- mulitple variant for product #1002
insert into public.product_variants(id, "name" , product_id ) values(3100022, 'var' 	, 1002);

--inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(601, 502, 3, now(), now(), 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(605, 501, 4, now(), now(), 99001, 400.0, 310001);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(602, 502, 8, now(), now(), 99001, 1200.0, 310003);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(604, 502, 6, now(), now(), 99001, 700.0, 310005);

insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(606, 501, 6, now(), now(), 99002, 600.0, 310002);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(607, 501, 1, now(), now(), 99002, 600.0, 3100022);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(608, 502, 8, now(), now(), 99002, 1200.0, 310007);


-- insert product images
INSERT INTO public.files (organization_id, url, "location", mimetype, orig_filename) VALUES(99001, 'cool_img.png', 'cool_img.png', 'image/png', 'cool_img.png');

INSERT INTO public.product_images (product_id, variant_id, "type", priority, uri) VALUES(1001, null, 7, 0, 'cool_img.png');
INSERT INTO public.product_images (product_id, variant_id, "type", priority, uri) VALUES(1001, 310001, 7, 0, 'cool_img.png');

