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
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ('product_1', 'product-one',101, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, barcode, cover_image) VALUES ( 'product_2', 'product-two',101, 201, 99002, now(), now(),'123456789', 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_3', 'product-three',101, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_4', 'product-four',102, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_5', 'product-five',102, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_6', 'product-six',102, 201, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_7', 'product-seven',101, 202, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_8', 'product-eight',102, 202, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ('product_1', 'product-one',101, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, barcode, cover_image) VALUES ( 'product_2', 'product-two',101, 201, 99002, now(), now(),'123456789', 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_3', 'product-three',101, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_4', 'product-four',102, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_5', 'product-five',102, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_6', 'product-six',102, 201, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_7', 'product-seven',101, 202, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_8', 'product-eight',102, 202, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ('product_1', 'product-one',101, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, barcode, cover_image) VALUES ( 'product_2', 'product-two',101, 201, 99002, now(), now(),'123456789', 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_3', 'product-three',101, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_4', 'product-four',102, 201, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_5', 'product-five',102, 202, 99001, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_6', 'product-six',102, 201, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_7', 'product-seven',101, 202, 99002, now(), now(), 'my_cool_img.jpg');
INSERT INTO public.products( name, p_name, brand_id, category_id, organization_id, created_at, updated_at, cover_image) VALUES ( 'product_8', 'product-eight',102, 202, 99002, now(), now(), 'my_cool_img.jpg');



--DROP SEQUENCE public.hibernate_sequence;
--
--CREATE SEQUENCE public.hibernate_sequence
--	INCREMENT BY 1
--	MINVALUE 1
--	MAXVALUE 9223372036854775807
--	START 1
--	CACHE 1
--	NO CYCLE;
