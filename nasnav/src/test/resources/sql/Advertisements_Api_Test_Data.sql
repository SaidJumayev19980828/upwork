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


INSERT INTO public.bank_accounts(id, created_At, org_id, user_id, wallet_Address, opening_Balance,
                                 opening_Balance_Activity_id, opening_Balance_Date, locked)
values (10, now(), 99001, null, 'address', 14, null, now(), false);



INSERT INTO public.bank_account_activities(
	id, account_id, amount_in)
	VALUES (1, 10, 14);

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

INSERT INTO public.advertisements (id, created_at)
VALUES (1001, now()) on conflict do nothing;

INSERT INTO public.advertisements (id, created_at)
VALUES (1002, now()) on conflict do nothing;

INSERT INTO public.advertisements (id, created_at)
VALUES (1003, now()) on conflict do nothing;

INSERT INTO public.advertisements (id, created_at, organization_id)
VALUES (1004, now(), 99002) on conflict do nothing;

INSERT INTO public.advertisements (id, created_at, from_date, to_date, banner_url, organization_id)
VALUES (1005, now(), now(), now(), 'bannessssssssr_url', 99002) on conflict do nothing;

INSERT INTO public.advertisements (id, name, created_at, from_date, to_date, banner_url, organization_id)
VALUES (1006, 'adv1', now(), CURRENT_DATE - INTERVAL '12 day', CURRENT_DATE + INTERVAL '12 day', 'bannessssssssr_url', 99002) on conflict do nothing;



INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1001, 100, 3000, 1001, 1005);
INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1002, 100, 3000, 1002, 1005);
INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1003, 100, 3000, 1003, 1005);


INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1013, 100, 3000, 1006, 1004);

INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1014, 100, 3000, 1001, 1004);


INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1004, 100, 3000, 1001, 1006);
INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1005, 100, 3000, 1002, 1006);
INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1006, 100, 3000, 1003, 1006);


INSERT INTO public.employee_users(id, email, organization_id, authentication_token, shop_id)
VALUES (88, 'testuser1@nasnav.com', 99001, '101112', null);

INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com', 'user1', '1', 99001) on CONFLICT do nothing;

insert into public.roles(id, name, organization_id)
values (1, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name, organization_id)
values (2, 'ORGANIZATION_MANAGER', 99001);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id)
VALUES (20, 88, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id)
VALUES (21, 88, 2);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id)
VALUES (700003, '1', now(), 88, null) on CONFLICT do nothing;


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


INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (99, 'user99@nasnav.com', 'user6', '99', 99002) on CONFLICT do nothing;

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id)
VALUES (70009, '99', now(), null, 99) on CONFLICT do nothing;

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


INSERT INTO public.posts(id, user_id, org_id, type, status, advertisement_id)
values (1999, 99, 99002, 0, 1, 1006) on CONFLICT do nothing;

INSERT INTO public.sub_posts(
	id, post_id, product_id)
	VALUES (1, 1006, 1001),
           (9, 1999, 1008),
	       (2,1006,1002) on conflict do nothing;



INSERT INTO public.compensation_action(
	id, name, description)
	VALUES (1, 'LIKE', 'test description for LIKE'),
           (2, 'JOIN_EVENT', 'test description for Join Event');

INSERT INTO public.compensation_rules(
	id, name, action_id, organization_id, is_active)
	VALUES (1, 'rule test', 1, 99001, true);


INSERT INTO public.compensation_rule_tier(
	id, condition, reward, is_active, rule_id)
	VALUES (1, 1, 10.0, true, 1),
           (2, 2, 20.0, true, 1);


INSERT INTO public.post_likes(id, user_id, created_at ,sub_post_id)
values (1001, 94 , now(),1) ;

INSERT INTO public.post_likes(id, user_id, created_at ,sub_post_id)
values (1003, 92 , now(),1) ;

INSERT INTO public.advertisement_product_compensation(
	id, advertisement_product_id, compensation_rule)
	VALUES (1, 1014, 1);


INSERT INTO public.bank_accounts(id, created_At, org_id, user_id, wallet_Address, opening_Balance,
                                 opening_Balance_Activity_id, opening_Balance_Date, locked)
values (11, now(), null, 93, 'address2', 10, null, now(), false);