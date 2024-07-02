
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'fortune');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting organization domain
INSERT INTO public.organization_domains (id, "domain", organization_id, canonical) VALUES(150001, 'fortune.nasnav.com', 99001, 0);
INSERT INTO public.organization_domains (id, "domain", organization_id, canonical) VALUES(150002, 'www.fortune.com', 99001, 1);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 201, 'brand_3', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (104, 201, 'brand_4', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 102, 99001, 0);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99002);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99002, '123456',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '123456', now(), 70, null);
--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 2);


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



INSERT INTO public.compensation_action(
	id, name, description)
	VALUES (1, 'SHARE', 'test description for share'),
           (2, 'JOIN_EVENT', 'test description for Join Event');

INSERT INTO public.compensation_rules(
	id, name, action_id, organization_id, is_active)
	VALUES (1, 'rule test', 1, 99001, true),
	       (12, 'rule test2', 1, 99001, true);


INSERT INTO public.compensation_rule_tier(
	id, condition, reward, is_active, rule_id)
	VALUES (1, 10, 10.0, true, 1),
	       (2, 20, 20.0, true, 1),
	       (3, 10, 20.0, true, 12),
	       (4, 20, 20.0, true, 12);


INSERT INTO public.advertisements (id, created_at,from_date, to_date)
VALUES (1001, now() , now() - INTERVAL '2 DAY', now() + INTERVAL '5 DAY' ) on conflict do nothing;


INSERT INTO public.advertisement_product (id, coins, likes, product_id, advertisement_id)
VALUES (1001, 100, 3000, 1001, 1001);

INSERT INTO public.advertisement_product_compensation(
	id, advertisement_product_id, compensation_rule)
	VALUES (1, 1001, 12);
