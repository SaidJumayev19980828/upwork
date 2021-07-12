----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_3', 101, 99001, 0);

--insering users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99002, '161718',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser4@nasnav.com', 99001, '192021',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (73, 'testuser6@nasnav.com', 99001, 'TTTRRR',  502);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100001, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100002, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100003, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100004, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (100005, 'TTTRRR', now(), 73, null);

--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 5);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (25, 73, 6);




--insert product features
INSERT INTO public.product_features (id, name, p_name, description, organization_id) VALUES(7001, 'Color', 'color', '', 99001);
INSERT INTO public.product_features (id, name, p_name, description, organization_id) VALUES(7002, 'Size', 'size', '', 99001);



--inserting products
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1', 'product-one',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2', 'product-two',101, 201, 99002, now(), now(),'123456789');
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3', 'product-three',101, 202, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4', 'product-four',102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5', 'product-five',102, 202, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6', 'product-six',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7', 'product-seven',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8', 'product-eight',102, 202, 99002, now(), now());



-- variants for each product
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code, weight) values(310001, 'var' 	, 1001, '54852222s' ,'sfasd223' ,'111-111', 5.5);
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code) values(310002, 'var' 	, 1002, '5564688', 'qwe1121' ,'111-112');
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code) values(310003, 'var' 	, 1003, 'erdsfd587' ,'zxc213' ,'111-113');
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code) values(310005, 'var' 	, 1005, 'sdfsd5f8' ,'fgh1232' ,'111-114' );
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code) values(310006, 'var' 	, 1006, 'sfds5f8' ,'tyr1212' ,'111-115');
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code) values(310007, 'var' 	, 1007, 'sdfsd587' ,'uiuyrewr' ,'111-116');
insert into public.product_variants(id, "name" , product_id, barcode ,sku, product_code) values(310008, 'var' 	, 1008, '5564sdfsd' ,'bvn2322', '111-117');

update public.product_variants 
set feature_spec = '{"7001":"Blue", "7002":"XL"}'
where id = 310005;

-- mulitple variant for product #1002
insert into public.product_variants(id, "name" , product_id ) values(3100022, 'var' 	, 1002);

INSERT INTO public.units VALUES(111001, 'm') ;

--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 3, 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(605, 501, 4, 99001, 400.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 8, 99001, 1200.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 502, 6, 99001, 700.0, 310005);

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(606, 501, 6, 99002, 600.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(607, 501, 1, 99002, 600.0, 3100022);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, unit_id) values(608, 502, 8, 99002, 1200.0, 310007, 111001);


--inserting tags
insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5001, 'tag_1', 'tag_1', 201, 99001, '');
insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5002, 'tag_2', 'tag_2', 201, 99002, '');
insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5003, 'tag_3', 'tag_2', 201, 99002, '');

insert into product_tags(product_id, tag_id) values(1001, 5001);
insert into product_tags(product_id, tag_id) values(1001, 5002);
insert into product_tags(product_id, tag_id) values(1002, 5001);
insert into product_tags(product_id, tag_id) values(1003, 5001);
insert into product_tags(product_id, tag_id) values(1004, 5001);
insert into product_tags(product_id, tag_id) values(1005, 5001);
insert into product_tags(product_id, tag_id) values(1006, 5001);
insert into product_tags(product_id, tag_id) values(1007, 5001);
insert into product_tags(product_id, tag_id) values(1008, 5001);

-- insertign extra attributes
INSERT INTO public.extra_attributes (id, key_name, attribute_type, organization_id, icon) VALUES(100001, 'Extra Prop', 'String'::character varying, 99001, null);
INSERT INTO public.extra_attributes (id, key_name, attribute_type, organization_id, icon) VALUES(100002, 'more data', 'String'::character varying, 99001, null);
INSERT INTO public.extra_attributes (id, key_name, attribute_type, organization_id, icon) VALUES(100003, 'EXTRA EXTRA', 'String'::character varying, 99002, null);
INSERT INTO public.extra_attributes (id, key_name, attribute_type, organization_id, icon) VALUES(100004, 'SPECs', 'String'::character varying, 99002, null);


INSERT INTO public.products_extra_attributes (extra_attribute_id, value, variant_id) VALUES(100001, 'extra data', 310001);
INSERT INTO public.products_extra_attributes (extra_attribute_id, value, variant_id) VALUES(100001, 'good info', 310002);
INSERT INTO public.products_extra_attributes (extra_attribute_id, value, variant_id) VALUES(100002, 'bla bla', 310001);
INSERT INTO public.products_extra_attributes (extra_attribute_id, value, variant_id) VALUES(100004, 'yep!', 310002);
INSERT INTO public.products_extra_attributes (extra_attribute_id, value, variant_id) VALUES(100003, 'bla bla', 310007);
INSERT INTO public.products_extra_attributes (extra_attribute_id, value, variant_id) VALUES(100004, 'cool!', 310007);



