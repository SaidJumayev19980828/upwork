INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(81800,'Egypt', 81800, 'EGP');

--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 81800);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 81800);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (2101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (2102, 201, 'brand_2', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (5501, 'shop_1', 2102, 99001);
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (5502, 'shop_2', 2101, 99001);

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

-- insert tags
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22001, 201, 'squishy things', 'squishy things', 'squishy_things', '{}', 0, 99001);
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22002, 202, 'mountain equipment', 'mountain equipment', 'mountain_equipment', '{}', 0, 99001);

-- insert product_tags
insert into public.product_tags(product_id, tag_id) values(1001, 22001);
insert into public.product_tags(product_id, tag_id) values(1004, 22002);

-- insert promotions
INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99001, 'promotion1', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '12345',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_brands":[2101]}', '{"amount":1}', 6801, now(), 6,2, 5);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by,created_on, class_id, type_id, priority)
VALUES (99002, 'promotion2', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '10 DAY', 1, 2, null,
        '{"applied_to_tags":[22002],"discount_value_max":1,"cart_amount_min":1}', '{}', 6901, now() ,3 ,5, 3);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99003, 'promotion3', 99001, now() - INTERVAL '5 DAY', now() + INTERVAL '7 DAY', 1, 1, '12345',
        '{"applied_to_products":[1001]}', '{"amount":1}', 6801, now(), 6, 6, 8);

INSERT INTO public.promotions(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains,
                              discount, created_by, created_on, class_id, type_id, priority)
VALUES (99004, 'promotion4', 99001, now() - INTERVAL '5 DAY', now() - INTERVAL '1 DAY', 2, 1, '12345',
        '{"discount_value_max":1,"cart_amount_min":1,"applied_to_brands":[2101]}', '{"amount":1}', 6801, now(), 6,2, 999);