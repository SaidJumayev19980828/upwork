INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
INSERT INTO public.addresses(id, address_line_1, phone_number, area_id) values(12300001, 'Ali papa cave', 0, 1);

INSERT INTO public.organizations(id, name, p_name, currency_iso, yeshtery_state) VALUES (99001, 'yeshtery', 'yeshtery', 818, 1);

INSERT INTO public.settings(id, setting_name, setting_value, organization_id, type)
VALUES (99001, 'RETURN_DAYS_LIMIT', '0', 99001, 0);

INSERT into public.organization_payments(id,organization_id ,gateway, account ) values(100025, 99001, 'cod', 'dummy');

INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99001);

INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (501, 'shop_1', 101, 99001, 0, 12300001, 'code1', false);

INSERT INTO public.loyalty_tier(id, tier_name, is_active, created_at, organization_id, constraints)
    VALUES (1, 'default_tier', true, now(), 99001, '{"ORDER_ONLINE":0.05, "REFERRAL": 0.01, "PICKUP_FROM_SHOP": 0.01}');

insert into public.loyalty_point_config
    values (31001, 'description', 99001, 501, true, now(), 1,
        '{"ORDER_ONLINE":{"ratio_from":7, "ratio_to":1}, "REFERRAL":{"ratio_from":7, "ratio_to":1, "amount": 100}, "PICKUP_FROM_SHOP":{"ratio_from":7, "ratio_to":1, "amount": 100}}');

INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'yuser1@nasnav.com','yuser1','456', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id, tier_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001, 88, 1);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123', now(), null, 88);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (7, '161718', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (8, 'abcdefg', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (9, '192021', now(), 71, null);

insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);
insert into roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 6);

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id, removed ) values(310001, 'var' 	, 1001, 0);

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 6, 99001, 600.00, 310001);

INSERT INTO public.organization_shipping_service values('TEST', 99001, '{ "name": "Shop","type": "long","value": "14" }', 99001);
INSERT INTO public.organization_shipping_service values('PICKUP', 99001, '{"ALLOWED_SHOP_ID_LIST":[601]}', 99002);

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);