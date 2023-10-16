----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99001, 'organization_1', 818, 1);
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99002, 'organization_2', 818, 1);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 201, 'brand_2', 99002);


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_2', 101, 99002, 0);

-- insert employee user
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501);

-- insert user token
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '131415', now(), 69, null);

-- inserting promotions
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630001, 'WELCOME', 99002, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'WELCOME2020', '{"applied_to_products":[1001,1002]}', '{}', 69, now());
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630002, 'HI', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'GREEEEEED', '{"applied_to_tags":[22001,22002]}', '{"percentage":10}', 69, now());
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630003, 'GIVE_US_MONEY', 99001, now() - INTERVAL '10 min' , now() + INTERVAL '200 min', 1, 0, 'MONEY2020', '{"applied_to_brands":[101]}', '{"percentage":10}', 69, now());

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99002, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1009, 'product_9',103, 202, 99002, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id, feature_spec ) values(310001, 'var' 	, 1001, '{"234":"66"
}');
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id, feature_spec ) values(310004, 'var' 	, 1003, '{"234":"45"
}');
insert into public.product_variants(id, "name" , product_id ) values(310005, 'var' 	, 1004);
insert into public.product_variants(id, "name" , product_id ) values(310006, 'var' 	, 1006);
insert into public.product_variants(id, "name" , product_id ) values(310007, 'var' 	, 1007);
insert into public.product_variants(id, "name" , product_id ) values(310008, 'var' 	, 1009);
insert into public.product_variants(id, "name" , product_id ) values(310009, 'var' 	, 1008);
insert into public.product_variants(id, "name" , product_id ) values(310010, 'var' 	, 1008);

--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(601, 502, 6, 99001, 600.00, 310001, 1, 5);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(602, 501, 8, 99001, 1200.0, 310002, 1, 10);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(603, 501, 4, 99001, 300.00, 310003, 1, 10);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(604, 502, 6, 99001, 700.00, 310004, 1, 5);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(605, 502, 0, 99001, 700.00, 310005, 0, 20);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(606, 502, 1, 99001, 700.00, 310010, 1, 20);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(607, 503, 1, 99002, 700.00, 310008, 1, 20);

--inserting tags
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22001, 201,  'squishy things', 'squishy things', 'squishy_things', '{}', 0, 99001);
INSERT INTO public.tags (id, category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(22002, 202, 'mountain equipment', 'mountain equipment', 'mountain_equipment', '{}', 0, 99001);

-- insert product_tags
insert into public.product_tags(product_id, tag_id) values(1003, 22001);
insert into public.product_tags(product_id, tag_id) values(1004, 22002);