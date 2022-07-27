----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99001, 'organization_1', 818, 1);
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99002, 'organization_2', 818, 1);
INSERT INTO public.organizations(id, name, p_name, currency_iso, yeshtery_state) VALUES (99003, 'yeshtery', 'yeshtery', 818, 1);

--insert organization domain
INSERT INTO public.organization_domains
("domain", organization_id, subdir)
VALUES('develop.nasnav.org', 99001, null);


--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Shoe size', 's-size', 'Size of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Shoe color', 's-color', 'Color of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe size', 's-size', 'Size of the shoes', 99002);


INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888001, 1, 'Badr city', 99001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888002, 1, 'Badr city', 99002);



INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300001, 'address line', 1, '+1111234567', 888001);
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300002, 'address line', 1, '+1111234567', 888001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (501, 'shop_1', 102, 99001, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (502, 'shop_2', 101, 99002, 12300001);

--loyalty data
INSERT INTO public.loyalty_tier(id, tier_name, is_active, created_at, organization_id, coefficient)
    VALUES (1, 'default_tier', true, now(), 99001, 0.05);
INSERT INTO public.loyalty_tier(id, tier_name, is_active, created_at, organization_id, coefficient)
    VALUES (2, 'default_tier for org 2', true, now(), 99002, 0.05);
INSERT INTO public.loyalty_tier(id, tier_name, is_active, created_at, organization_id, coefficient)
    VALUES (3, 'default_tier for org 3', true, now(), 99003, 0.05);

insert into public.loyalty_point_config(id, description, organization_id, shop_id, is_active, created_at, ratio_from, ratio_to, coefficient, default_tier_id, expiry)
    values (31001, 'desctiption', 99001, null, true, now(), 7, 1, 0.5, 1, null);
insert into public.loyalty_point_config(id, description, organization_id, shop_id, is_active, created_at, ratio_from, ratio_to, coefficient, default_tier_id, expiry)
    values (31002, 'desctiption', 99002, null, true, now(), 7, 1, 0.5, 2, null);
insert into public.loyalty_point_config(id, description, organization_id, shop_id, is_active, created_at, ratio_from, ratio_to, coefficient, default_tier_id, expiry)
    values (31003, 'desctiption', 99003, null, true, now(), 7, 1, 0.5, 3, null);

INSERT INTO public.settings(id, setting_name, setting_value, organization_id, type)
    VALUES (99001, 'RETURN_DAYS_LIMIT', '0', 99001, 0);
INSERT INTO public.settings(id, setting_name, setting_value, organization_id, type)
    VALUES (99002, 'RETURN_DAYS_LIMIT', '0', 99002, 0);
INSERT INTO public.settings(id, setting_name, setting_value, organization_id, type)
VALUES (99003, 'RETURN_DAYS_LIMIT', '0', 99003, 0);

--insering employees
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'levis.nasnav@gmail.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'levis.galal@nasnav.com', 99002, '161718',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, user_status)
VALUES (71, 'org.manager@yeshtery.com', 99003, '13141516', 201);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '13141516', now(), 71, null);

--inserting yeshtery users
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id, tier_id)
VALUES (808, 'test4@nasnav.com','user1','123', 99003, 3);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id, tier_id)
VALUES (88, 'test2@nasnav.com','user1','123', 99001, 808, 1);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id, tier_id)
VALUES (89, 'test3@nasnav.com','user2','456', 99002, 808, 2);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id, tier_id)
VALUES (90, 'test4@nasnav.com','user2','789', 99003, 808, 3);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700005, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700006, '456', now(), null, 89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700007, '789', now(), null, 90);


--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(3, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(7, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(6, 'CUSTOMER', 99001);
insert into roles(id, name,  organization_id) values(8, 'ORGANIZATION_MANAGER', 99003);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 70, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (25, 71, 8);

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',101, 202, 99002, now(), now());


-- variants for each product
insert into public.product_variants(id, "name" , product_id) values(310001, 'var' , 1001);
insert into public.product_variants(id, "name" , product_id) values(310002, 'var2' , 1002);


--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(601, 501, 6, 99001, 600.00, 310001, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(602, 502, 4, 99002, 200.00, 310002, 1);


INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters)
values('BOSTA_LEVIS', 99001,
       '{
           "AUTH_TOKEN": "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357",
           "BUSINESS_ID": "yM1ngytZ0",
           "SERVER_URL": "https://stg-app.bosta.co/api/v0",
           "WEBHOOK_URL": "https://backend.nasnav.org/callbacks/shipping/service/BOSTA_LEVIS/99001",
           "CAIRO_PRICE": 25,
           "ALEXANDRIA_PRICE":30,
           "DELTA_CANAL_PRICE":30,
           "UPPER_EGYPT":45,
           "TRACKING_URL": "https://stg-app.bosta.co/api/tracking/26522"
        }');
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters)
values('BOSTA_LEVIS', 99002,
       '{
           "AUTH_TOKEN": "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357",
           "BUSINESS_ID": "yM1ngytZ0",
           "SERVER_URL": "https://stg-app.bosta.co/api/v0",
           "WEBHOOK_URL": "https://backend.nasnav.org/callbacks/shipping/service/BOSTA_LEVIS/99002",
           "CAIRO_PRICE": 25,
           "ALEXANDRIA_PRICE":30,
           "DELTA_CANAL_PRICE":30,
           "UPPER_EGYPT":45,
           "TRACKING_URL": "https://stg-app.bosta.co/api/tracking/26522"
        }');
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters)
values('BOSTA_LEVIS', 99003,
       '{
           "AUTH_TOKEN": "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357",
           "BUSINESS_ID": "yM1ngytZ0",
           "SERVER_URL": "https://stg-app.bosta.co/api/v0",
           "WEBHOOK_URL": "https://backend.nasnav.org/callbacks/shipping/service/BOSTA_LEVIS/99003",
           "CAIRO_PRICE": 25,
           "ALEXANDRIA_PRICE":30,
           "DELTA_CANAL_PRICE":30,
           "UPPER_EGYPT":45,
           "TRACKING_URL": "https://stg-app.bosta.co/api/tracking/26522"
        }');

--assign organization to cod payment
INSERT into public.organization_payments(id,organization_id ,gateway, account )
values(100025, 99001, 'cod', 'dummy');

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);
INSERT INTO public.User_addresses values(12300002, 89, 12300001, false);
INSERT INTO public.User_addresses values(12300003, 90, 12300001, false);