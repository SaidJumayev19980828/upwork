
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(2,'United States', 819, 'USD');
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(3,'Egypt', 818181, 'ABC');

--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'fortune');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);
-- Add Nasnav organization
INSERT INTO public.organizations(id, name, p_name, currency_iso) VALUES (99003, 'nasnav','nasnav', 818);
--Add Subscribed Organization
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99004, 'organization_3', 818);

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
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (90, 'user3@nasnav.com','user3','789', 99004);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (103, '789', now(), null,90);

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
    VALUES (70, 'testuser3@nasnav.com', 99002, '123456',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser4@nasnav.com', 99004, '124567',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '123456', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '124567', now(), 71, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 68, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 70, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 71, 2);

--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Shoe size', 's-size', 'Size of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Shoe color', 's-color', 'Color of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe size', 's-size', 'Size of the shoes', 99002);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(238,'size', 'size', 'Size', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

-- insert products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());

--inserting package
INSERT INTO public.package(id,name,description,price,period_in_days,currency_iso,stripe_price_id) values (99001,'test 1','test description',1500,30,818,'price_1NzLNBAV4qGEOW4EItZ5eE2p');
INSERT INTO public.package(id,name,description,price,period_in_days,currency_iso,stripe_price_id) values (99002,'test 2','test2 description',1900,30,819,'price_1NzLNBGCDqGEOW4EItZ5eE2p');
INSERT INTO public.package(id,name,description,price,period_in_days,currency_iso,stripe_price_id) values (99003,'test 3','package with wrong currency',1.9,30,818181,'price_1NzEFNBGR4qGEOW4EItZ5eE2p');
INSERT INTO public.package(id,name,description,price,period_in_days,currency_iso,stripe_price_id) values (99004,'test 4','package with missing currency',1.9,30,null,'price_1NzLNGHR4qGEOW4EItZ5eE2p');

--inserting service
INSERT INTO public.service(id,code,name,description) values (99001,'THREE_SIXTY_TEST','THREE_SIXTY','THREE_SIXTY Service');
INSERT INTO public.service(id,code,name,description) values (99002,'MET_AVERSE_TEST','MET_AVERSE','MET_AVERSE Service');
INSERT INTO public.service(id,code,name,description) values (99003,'CHAT_SERVICES_TEST','CHAT_SERVICES','CHAT_SERVICES Service');
INSERT INTO public.service(id,code,name,description) values (99004,'VIRTUAL_LANDS_TEST','VIRTUAL_LANDS','VIRTUAL_LANDS Service');

-- Join package & service
INSERT INTO public.package_service(package_id,service_id) values (99001,99001);
INSERT INTO public.package_service(package_id,service_id) values (99001,99002);

INSERT INTO public.package_service(package_id,service_id) values (99002,99003);
INSERT INTO public.package_service(package_id,service_id) values (99002,99004);

INSERT INTO public.package_service(package_id,service_id) values (99003,99003);
INSERT INTO public.package_service(package_id,service_id) values (99003,99004);

INSERT INTO public.package_registered(id, creator_employee_id, org_id, package_id, registered_date) values (201, 70, 99001, 99003, now());
INSERT INTO public.package_registered(id, creator_employee_id, org_id, package_id, registered_date) values (202, 70, 99004, 99003, now());


INSERT INTO public.bank_accounts(id,created_At,org_id,user_id,wallet_Address,opening_Balance,opening_Balance_Activity_id,opening_Balance_Date,locked)
values (10,now(),99002,null,'address',40000,null,now(),false);
-- Add Nasnav bank account
INSERT INTO public.bank_accounts(id,created_At,org_id,user_id,wallet_Address,opening_Balance,opening_Balance_Activity_id,opening_Balance_Date,locked)
values (11,now(),99003,null,'address',0,null,now(),false);

INSERT INTO public.subscription(id, type, payment_date, start_date, expiration_date, paid_amount, package_id, org_id,status,stripe_subscription_id)
values (10000011,'wert','2023-10-02 22:19:50.321129','2023-10-02 00:00:00',	'4023-10-02 00:00:00' , 3455.00,99002,99004,'active', null);

INSERT INTO public.subscription(id, type, payment_date, start_date, expiration_date, paid_amount, package_id, org_id,status,stripe_subscription_id)
values (10000012,'wert','2023-10-02 22:19:50.321129','2022-10-02 00:00:00',	'2022-10-02 00:00:00' , 3455.00,99002,99002,'active', null);