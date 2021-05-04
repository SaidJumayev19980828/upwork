
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

-- countries and addresses
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);

INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300001, 'address line', 1, '01111234567');
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300002, 'address line', 1, '01111234567');

INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id, removed) VALUES (501, 'shop', 101, 99001, 12300002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id, removed) VALUES (502, 'shop2', 101, 99001, 12300002, 0);


INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser3@nasnav.com', 99001, '123456',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
    VALUES (71, 'testuser4@nasnav.com', 99002, 'rtrtyy',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '123456', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, 'rtrtyy', now(), 71, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(3, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(4, 'CUSTOMER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 2);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'test2@nasnav.com','user2','456', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com','user1','789', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (91, 'test4@nasnav.com','user2','abc', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '456', now(), null, 89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '789', now(), null, 90);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, 'abc', now(), null, 91);

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);

-- inserting shipping service
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters, id)VALUES('TEST', 99001, '{"hotline":"19888"}', 11001);

-- inserting promotions
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on, type_id, priority)
VALUES(630000, 'HI', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'GREEEEEED', '{"cart_amount_min":0}', '{"percentage":60}', 69, now(), 0, 0);
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on, type_id)
VALUES(630001, 'Shipping promo', 99001, now(), now() + INTERVAL '200 DAY', 1, 0, null, '{"discount_value_max":100,"cart_amount_min":10}', '{"percentage":50}', 69, now(), 1);
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on, type_id)
VALUES(6300011, 'Shipping promo2', 99001, now(), now() + INTERVAL '200 DAY', 1, 0, null, '{"discount_value_max":100,"cart_amount_min":200}', '{"percentage":75}', 69, now(), 1);
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on, type_id, PRIORITY)
VALUES(630002, 'Total cart value promo', 99001, now(), now() + INTERVAL '200 DAY', 1, 0, null, '{"discount_value_max":200, "cart_amount_min":300}', '{"percentage":50}', 69, now(), 3, 3);
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on, type_id, PRIORITY)
VALUES(630003, 'Total cart items promo', 99001, now(), now() + INTERVAL '200 DAY', 1, 0, null, '{"discount_value_max":200,"cart_quantity_min":2}', '{"percentage":50}', 69, now(), 4, 2);
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on, type_id, PRIORITY)
VALUES(630004, 'buy X get Y promo', 99001, now(), now() + INTERVAL '200 DAY', 1, 0, null,
        '{"discount_value_max":100,
         "cart_quantity_min":2,
         "applied_to_products":{"required": 0,"ids":[1001]},
         "product_quantity_min" : 3,
         "product_to_give": 1}', null, 69, now(), 6, 1);
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on, type_id, priority)
VALUES(630006, 'buy X get Y promo2', 99001, now(), now() + INTERVAL '200 DAY', 1, 0, null,
        '{"discount_value_max":100,
         "cart_quantity_min":2,
         "applied_to_products":{"required": 0,"ids":[1001, 1002]},
         "product_quantity_min" : 10,
         "product_to_give": 5}', null, 69, now(), 6,0);
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630005, 'promo code', 99001, now(), now() + INTERVAL '200 DAY', 1, 0, 'LESS2020', '{"cart_amount_min":0}', '{"percentage":10}', 69, now());

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99001, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 99001, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id, feature_spec ) values(310001, 'var', 1001, '{"234":"66"}');
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1003);
insert into public.product_variants(id, "name" , product_id, feature_spec ) values(310004, 'var', 1004, '{"234":"45"}');

--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(601, 501, 50, 99001, 100.00, 310001, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(602, 502, 50, 99001, 100.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(603, 501, 50, 99001, 100.00, 310003, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(604, 501, 50, 99001, 100.00, 310004, 1);




