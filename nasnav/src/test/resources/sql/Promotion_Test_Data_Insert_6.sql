INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(81800,'Egypt', 81800, 'EGP');

--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 81800);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 81800);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (2101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (2102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (2103, 201, 'brand_3', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (5501, 'shop_1', 2102, 99001);
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (5502, 'shop_2', 2101, 99001);

--inserting users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
VALUES (6800, 'testcustomer@nasnav.com', 99001, '101111');

--insering employees
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (6801, 'testuser1@nasnav.com', 99001, '101112',  5502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (6901, 'testuser2@nasnav.com', 99002, '131415',  5501);

--inserting categories
INSERT INTO public.categories(id, name, logo, cover) VALUES (201, 'category_1', 'cool_cat.jpg', 'cool_cat_cover.jpg');
INSERT INTO public.categories(id, name, logo, cover) VALUES (202, 'category_2', 'cool_cat2.jpg', 'cool_cat_cover2.jpg');

 --insert product
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',2101, 201, 99001, now(), now() + INTERVAL '5 DAY');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',2101, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',2101, 202, 99001, now(), now() + INTERVAL '2 DAY');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',2102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',2102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',2102, 201, 99001, now(), now() + INTERVAL '5 DAY');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',2103, 201, 99002, now(), now());

-- insert tags
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22001, 201, 'test1', 'test1', 'squishy_things', '{}', 0, 99001);
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22002, 202, 'test2', 'test2', 'mountain_equipment', '{}', 0, 99001);
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22003, 202, 'test3', 'test3', 'test3', '{}', 0, 99001);

-- insert product_tags
insert into public.product_tags(product_id, tag_id) values(1001, 22001);
insert into public.product_tags(product_id, tag_id) values(1001, 22002);
insert into public.product_tags(product_id, tag_id) values(1001, 22003);
insert into public.product_tags(product_id, tag_id) values(1002, 22001);
insert into public.product_tags(product_id, tag_id) values(1002, 22002);
insert into public.product_tags(product_id, tag_id) values(1003, 22001);
insert into public.product_tags(product_id, tag_id) values(1003, 22003);
insert into public.product_tags(product_id, tag_id) values(1004, 22002);
insert into public.product_tags(product_id, tag_id) values(1004, 22003);
insert into public.product_tags(product_id, tag_id) values(1005, 22001);
insert into public.product_tags(product_id, tag_id) values(1006, 22002);
insert into public.product_tags(product_id, tag_id) values(1007, 22003);

-- insert promotions
INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99001, 'promotion1invalid', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '1',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_brands":[2101],"applied_to_users":[101111]}', '{"amount":1}', 6801, now(), 6,2, 5);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99002, 'promotion2invalid', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '2',
        '{"discount_value_max":1,"cart_amount_min":1}', '{"amount":1}', 6801, now(), 6,0, 5);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99003, 'promotion3invalid', 99001, now() + INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '3',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_brands":[2101]}', '{"amount":1}', 6801, now(), 6,2, 5);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99004, 'promotion4invalid', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 0, 1, '4',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_brands":[2101],"applied_to_users":[101111]}', '{"amount":1}', 6801, now(), 6,2, 5);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99005, 'promotion5invalid', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 2, 1, '4',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_brands":[2101],"applied_to_users":[101111]}', '{"amount":1}', 6801, now(), 6,2, 5);




INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99006, 'promotion6', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '5',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_brands":[2101,2102]}', '{"amount":1}', 6801, now(), 6,7, 9);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99007, 'promotion7', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '6',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_tags":[2201,2202]}', '{"amount":1}', 6801, now(), 6,8, 8);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99008, 'promotion8', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '6',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_products":[1002,1005]}', '{"amount":1}', 6801, now(), 6,9, 7);