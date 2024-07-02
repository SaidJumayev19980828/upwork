----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99001, 'organization_1', 818, 1);
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99002, 'organization_2', 818, 1);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);

--insering users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99002, '161718',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser4@nasnav.com', 99001, '192021',  502);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100001, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100002, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100003, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100004, '192021', now(), 71, null);

--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 5);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 2);


--insert products
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1', 'product-one',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2', 'product-two',101, 201, 99002, now(), now(),'123456789');


insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code ) values(310001, 'var' 	, 1001, '54852222s' ,'sfasd223' ,'111-111');
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code) values(310002, 'var' 	, 1002, '5564688', 'qwe1121' ,'111-112');

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 3, 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(606, 501, 6, 99002, 600.0, 310002);


-- insert tags
insert into public.Tags(id, name, alias, category_id, organization_id, metadata) values(5001, 'tag_1', 'tag_1', 201, 99001, '');
insert into public.Tags(id, name, alias, category_id, organization_id, metadata) values(5002, 'tag_2', 'tag_2', 201, 99002, '');
insert into public.Tags(id, name, alias, category_id, organization_id, metadata) values(5003, 'tag_3', 'tag_3', 202, 99002, '');


-- insert seo keywords
insert into public.seo_keywords(id, entity_id, type_id, organization_id, keyword) values(333001, 99002, 0, 99002, 'Search bot cookie!');
insert into public.seo_keywords(id, entity_id, type_id, organization_id, keyword) values(333002, 1002, 1, 99002, 'Search bot choco!');
insert into public.seo_keywords(id, entity_id, type_id, organization_id, keyword) values(333003, 5001, 2, 99001, 'Search bot konafa with mango!');
insert into public.seo_keywords(id, entity_id, type_id, organization_id, keyword) values(333004, 5002, 2, 99002, 'Search bot notella!');
insert into public.seo_keywords(id, entity_id, type_id, organization_id, keyword) values(333005, 5003, 2, 99002, 'Search bot dark chocolate bars!');
insert into public.seo_keywords(id, entity_id, type_id, organization_id, keyword) values(333006, 201, 3, 99002, 'Category Keyword!');