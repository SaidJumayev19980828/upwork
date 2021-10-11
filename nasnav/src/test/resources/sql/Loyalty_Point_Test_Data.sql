INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
INSERT INTO public.addresses(id, address_line_1, phone_number, area_id) values(12300001, 'Ali papa cave', 0, 1);

INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);


INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99001);

INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (501, 'shop_1', 101, 99001, 0, 12300001, 'code1', false);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (502, 'shop_2', 101, 99001, 0, 12300001, 'code2', false);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (503, 'shop_3', 101, 99001, 0, 12300001, 'code3', false);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (504, 'shop_4', 101, 99002, 0, 12300001, 'code4', false);

INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '456', now(), null, 89);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  5010000000000000);

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
insert into public.product_variants(id, "name" , product_id, removed ) values(310001, 'var' 	, 1001, 1);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1001);

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 6, 99001, 600.00, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 501, 8, 99001, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 501, 4, 99001, 200.00, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 502, 6, 99001, 600.00, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(605, 502, 8, 99001, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(606, 502, 4, 99001, 200.00, 310003);

INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status, grand_total) VALUES(31001 , now(), 88, 99001, 8, 400);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, meta_order_id, address_id, total)
    values(33001, 88, now(), now(), 99001, 8, 501, 31001, 12300001, 400);

INSERT INTO public.shipment
(sub_order_id, shipping_service_id, parameters, created_at, updated_at, status, shipping_fee)
VALUES(33001, 'TEST', '{"Shop Id":501}' , now(), now(), 0, 20.0);

INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency, item_data) VALUES(33001, 601, 10, 400.0, 1,'{"name":"original"}');

INSERT INTO public.organization_shipping_service values('TEST', 99001, '{ "name": "Shop","type": "long","value": "14" }', 99001);

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);

insert into public.loyalty_point_types values (31001, 'old name');

insert into public.loyalty_point_config values (31001, 'desctiption', 99001, 501, 0, 500, 500, true, now());

insert into public.loyalty_points values (31001, 'desctiption_1', 99001, 31001, 500, 50, now(), now() + interval '30 day');
insert into public.loyalty_points values (31002, 'desctiption_2', 99001, 31001, 1000, 110, now(), now() + interval '30 day');

INSERT INTO public.booster
(booster_name, linked_family_member, number_family_children, review_products, number_purchase_offline, social_media_reviews, organization_id, level_booster, activation_months, id, is_active, purchase_size)
VALUES('test booster', 0, 0, NULL, 0, NULL, 99001, 0, 0, 199001, true, NULL);
