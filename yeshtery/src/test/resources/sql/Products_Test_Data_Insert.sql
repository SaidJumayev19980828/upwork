----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99001, 'organization_1', 818, 1);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name, logo, cover) VALUES (201, 'category_1', 'cool_cat.jpg', 'cool_cat_cover.jpg');
INSERT INTO public.categories(id, name, logo, cover) VALUES (202, 'category_2', 'cool_cat2.jpg', 'cool_cat_cover2.jpg');

INSERT INTO public.categories(id, name, parent_id) VALUES (203, 'category_1_1', 201);
INSERT INTO public.categories(id, name, parent_id) VALUES (204, 'category_1_2', 201);
INSERT INTO public.categories(id, name, parent_id) VALUES (205, 'category_2_1', 202);
INSERT INTO public.categories(id, name, parent_id) VALUES (206, 'category_2_2', 202);

INSERT INTO public.categories(id, name, parent_id) VALUES (207, 'category_3_1_cyclic', 201);
INSERT INTO public.categories(id, name, parent_id) VALUES (208, 'category_3_2_cyclic', 207);
update public.categories set parent_id = 208 where id = 207;

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);

--insering users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99001, '8895ssf',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '8895ssf', now(), 70, null);

--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 2);


--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Lispstick Color', 'lipstick_color', 'whatever', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Lipstick flavour', 'lipstick_flavour', 'bla bla bla', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe material', 's-material', 'Material of the shoes', 99001);




--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now() + INTERVAL '1 DAY');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99002, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99001, now(), now() + INTERVAL '2 DAY');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 99001, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310001, 'var' 	, 1001, 'ABCD1234', '{"234":"40", "235":"pink"}', 'code gensis', '1144');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310002, 'var' 	, 1002, 'EFGH1234', '{"234":"41", "235":"grey"}', 'code gelsis', '5522');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310003, 'var' 	, 1003, 'HIJK1234', '{"234":"42", "235":"black"}', 'code gefecis', '9966');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310004, 'var' 	, 1004, 'LMNO1234', '{"234":"43", "235":"white"}', 'code gamosis', '8855');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310005, 'var' 	, 1001, 'TRYU1234', '{"234":"44", "235":"brown"}', 'code golisis', '3366');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310006, 'var' 	, 1001, 'ASDFG234', '{"234":"45", "235":"blue"}', 'code gekomis', '7569');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310007, 'var' 	, 1001, 'QWER1234', '{"234":"46", "235":"lattece heart"}', 'code gakotis', '4521');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec, product_code, sku ) values(310008, 'var' 	, 1001, 'ZCXV1234', '{"234":"47", "235":"elephant tooth"}', 'code g', '3654');

--inserting additional variants
INSERT INTO public.product_variants(id,product_id, feature_spec, name, p_name, description, barcode)
VALUES(80001,1002, '{"234": 20, "235": "white"}', 'orginal variant', 'orginal_variant', 'we need to update this in tests', 'BCF559354');




--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 6, 99002, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 501, 8, 99002, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 501, 4, 99001, 200.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 502, 6, 99001, 700.0, 310004);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(605, 502, 6, 99001, 700.0, 310005);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(606, 502, 6, 99001, 700.0, 310006);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(607, 502, 6, 99001, 700.0, 310007);


INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22001, 201, 'squishy things', 'squishy things', 'squishy_things', '{}', 0, 99001);
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22002, 202, 'mountain equipment', 'mountain equipment', 'mountain_equipment', '{}', 0, 99001);

INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50011,22001);
INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50012,22002);


insert into tag_graph_edges(id, parent_id, child_id) values(60012, 50011, 50012);

insert into public.product_tags(product_id, tag_id) values(1001, 22001);
insert into public.product_tags(product_id, tag_id) values(1004, 22002);

