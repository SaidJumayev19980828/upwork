----------------------------inserting dummy data----------------------------
INSERT INTO public.countries(id, "name", iso_code, currency)
VALUES (1, 'Egypt', 818, 'EGP') on CONFLICT do NOTHING;
INSERT INTO public.cities(id, country_id, "name")
VALUES (1, 1, 'Cairo') on CONFLICT do NOTHING;
INSERT INTO public.cities(id, country_id, "name")
VALUES (3, 1, 'Alexandria') on CONFLICT do NOTHING;
INSERT INTO public.areas(id, "name", city_id)
VALUES (1, 'New Cairo', 1) on CONFLICT do NOTHING;
INSERT INTO public.areas(id, "name", city_id)
VALUES (144, 'Abu Kir', 3) on CONFLICT do NOTHING;

--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso)
VALUES (99001, 'organization_1', 818) on CONFLICT do NOTHING;
INSERT INTO public.organizations(id, name, currency_iso)
VALUES (99002, 'organization_2', 818) on CONFLICT do NOTHING;

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id)
VALUES (101, 202, 'brand_1', 99002) on CONFLICT do NOTHING;
INSERT INTO public.brands(id, category_id, name, organization_id)
VALUES (102, 201, 'brand_2', 99001) on CONFLICT do NOTHING;

--inserting categories
INSERT INTO public.categories(id, name)
VALUES (201, 'category_1') on CONFLICT do NOTHING;
INSERT INTO public.categories(id, name)
VALUES (202, 'category_2') on CONFLICT do NOTHING;

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1001, 'product_1', 101, 201, 99001, now(), now()) on conflict do nothing;
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode)
VALUES (1002, 'product_2', 101, 201, 99002, now(), now(), '123456789') on conflict do nothing;
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1003, 'product_3', 101, 202, 99001, now(), now()) on conflict do nothing;
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1004, 'product_4', 102, 201, 99001, now(), now()) on conflict do nothing;
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1005, 'product_5', 102, 202, 99001, now(), now()) on conflict do nothing;
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1006, 'product_6', 102, 201, 99002, now(), now()) on conflict do nothing;
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1007, 'product_7', 101, 202, 99002, now(), now()) on conflict do nothing;
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1008, 'product_8', 102, 202, 99002, now(), now()) on conflict do nothing;

INSERT INTO public.advertisements (id, coins, created_at, from_date, to_date, likes, product_id)
VALUES (1001, 100, now(), now() - INTERVAL '20 DAY', now() + INTERVAL '20 YEAR', 3, 1001) on conflict do nothing;

INSERT INTO public.advertisements (id, coins, created_at, from_date, to_date, likes, product_id)
VALUES (1002, 100, now(), now() - INTERVAL '20 DAY', now() + INTERVAL '10 YEAR', 2000, 1002) on conflict do nothing;

INSERT INTO public.advertisements (id, coins, created_at, from_date, to_date, likes, product_id)
VALUES (1003, 100, now(), now() - INTERVAL '20 DAY', now() - INTERVAL '10 YEAR', 2000, 1003) on conflict do nothing;

INSERT INTO public.advertisements (id, coins, created_at, from_date, to_date, likes, product_id)
VALUES (1004, 100, now(), now() - INTERVAL '20 DAY', now() - INTERVAL '20 YEAR', 2000, 1004) on conflict do nothing;



INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com', 'user1', '1', 99001) on CONFLICT do nothing;

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id)
VALUES (700003, '1', now(), null, 88) on CONFLICT do nothing;


INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (89, 'user1@nasnav.com', 'user1', '2', 99001) on CONFLICT do nothing;
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (90, 'user2@nasnav.com', 'user2', '3', 99001) on CONFLICT do nothing;

INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (91, 'user3@nasnav.com', 'user3', '4', 99001) on CONFLICT do nothing;

INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (92, 'user4@nasnav.com', 'user4', '5', 99001) on CONFLICT do nothing;
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (93, 'user5@nasnav.com', 'user5', '6', 99001) on CONFLICT do nothing;
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (94, 'user6@nasnav.com', 'user6', '7', 99001) on CONFLICT do nothing;


INSERT INTO public.posts(id, user_id, org_id, type, status, advertisement_id)
values (1001, 88, 99001, 0, 1, 1001) on CONFLICT do nothing;


INSERT INTO public.posts(id, user_id, org_id, type, status, advertisement_id)
values (1002, 89, 99001, 0, 1, 1002) on CONFLICT do nothing;

INSERT INTO public.posts(id, user_id, org_id, type, status, advertisement_id)
values (1003, 90, 99001, 0, 1, 1002) on CONFLICT do nothing;


INSERT INTO public.posts(id, user_id, org_id, type, status, advertisement_id)
values (1004, 91, 99001, 0, 1, 1003) on CONFLICT do nothing;



INSERT INTO public.posts(id, user_id, org_id, type, status, advertisement_id)
values (1005, 92, 99001, 0, 1, 1004) on CONFLICT do nothing;

INSERT INTO public.posts(id, user_id, org_id, type, status, advertisement_id)
values (1006, 93, 99001, 0, 1, 1004) on CONFLICT do nothing;



INSERT INTO public.post_likes(id, user_id, post_id, created_at)
values (1001, 94, 1001, now()) on CONFLICT do nothing;

INSERT INTO public.post_likes(id, user_id, post_id, created_at)
values (1002, 94, 1001, now()) on CONFLICT do nothing;

INSERT INTO public.post_likes(id, user_id, post_id, created_at)
values (1003, 94, 1001, now()) on CONFLICT do nothing;
INSERT INTO public.post_likes(id, user_id, post_id, created_at)
values (1004, 94, 1001, now()) on CONFLICT do nothing;

INSERT INTO public.post_likes(id, user_id, post_id, created_at)
values (1005, 94, 1001, now()) on CONFLICT do nothing;


INSERT INTO public.post_likes(id, user_id, post_id, created_at)
values (1006, 94, 1001, now()) on CONFLICT do nothing;



