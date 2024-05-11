--inserting organizations

INSERT INTO public.countries(id,name,iso_code,currency) values (100001, 'Egypt', 818, 'EGP');
INSERT INTO public.countries(id,name,iso_code,currency) values (100002, 'UK', 820, 'EGP');

INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'org-number-one');
INSERT INTO public.organizations(id, name,  p_name, extra_info, matomo) VALUES (99002, 'organization_2', 'org-number-two', '{"ALLOWED_COUNTRIES":[]}', 10);
INSERT INTO public.organizations(id, name,  p_name, extra_info) VALUES (99003, 'organization_3', 'org-number-three', '{"ALLOWED_COUNTRIES":[100002]}');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99004, 'organization_4', 818);

-- dummy brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99004);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 201, 'brand_3', 99003);


-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100001 , 'Bundle Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100002 , 'another Shop'  , 99002);
INSERT INTO public.shops (id,"name",  organization_id, is_warehouse) values(100003 , 'warehouse', 99001, 1);
INSERT INTO public.shops(id, name, brand_id, organization_id, removed) VALUES (501, 'shop_1', 102, 99004, 0);
INSERT INTO public.shops(id, name, brand_id, organization_id, removed) VALUES (502, 'shop_2', 103, 99003, 0);


----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (701, 'size', 'boolean', 99002, '/uploads/category/fearutes/feature1.jpg');
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (702, 'filter', 'boolean', 99001, '/uploads/category/logo/logo1.jpg');



INSERT INTO public.cities(id, country_id, name) values(100001, 100001,'Cairo');
INSERT INTO public.cities(id, country_id, name) values(100002, 100001,'Giza');
INSERT INTO public.cities(id, country_id, name) values(100003, 100002,'London');

INSERT INTO public.areas(id, name, city_id) values(100001, 'new cairo', 100001);
INSERT INTO public.areas(id, name, city_id) values(100002, 'Nasr city', 100001);
INSERT INTO public.areas(id, name, city_id) values(100003, 'Mohandiseen', 100002);
INSERT INTO public.areas(id, name, city_id) values(100004, 'Dokki', 100002);
INSERT INTO public.areas(id, name, city_id) values(100005, 'Agoza', 100002);
INSERT INTO public.areas(id, name, city_id) values(100006, 'Yorkshire', 100003);

insert into public.sub_areas ("id",area_id, "name", organization_id) values (888001, 100001, 'Badr city', 99001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888002, 100001, 'Future City', 99001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888003, 100001, 'WerWer Land', 99001);




--insert employee
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  100001);
INSERT INTO public.employee_users(id, email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser4@nasnav.com', 99004, '124567', 100002);
INSERT INTO public.employee_users(id, email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99004, '123456', 501);
INSERT INTO public.employee_users(id, email, organization_id, authentication_token, shop_id)
VALUES (78, 'testuser987@nasnav.com', 99003, '789987', 502);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);


--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'STORE_MANAGER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);

--inserting packages
INSERT INTO public.package(id, name, description, price, period_in_days, currency_iso, stripe_price_id)
values (99004, 'test 4', 'package with missing currency', 1.9, 30, null, 'price_1NzLNGHR4qGEOW4EItZ5eE2p');

--inserting services
INSERT INTO public.service(id, code, name, description)
values (99004, 'VIRTUAL_LANDS_TEST', 'VIRTUAL_LANDS', 'VIRTUAL_LANDS Service');

--inserting package-service relation
INSERT INTO public.package_service(package_id, service_id)
values (99004, 99004);

--register package to organization
INSERT INTO public.package_registered(id, creator_employee_id, org_id, package_id, registered_date)
values (202, 70, 99004, 99004, now());
INSERT INTO public.package_registered(id, creator_employee_id, org_id, package_id, registered_date)
values (203, 78, 99003, 99004, now());

--inserting subscription info
INSERT INTO public.subscription(id, type, payment_date, start_date, expiration_date, paid_amount, package_id, org_id, status, stripe_subscription_id)
values (10000011, 'wert', '2023-10-02 22:19:50.321129', '2025-10-02 00:00:00', '4023-10-02 00:00:00', 3455.00, 99004, 99004, 'active', null);
INSERT INTO public.subscription(id, type, payment_date, start_date, expiration_date, paid_amount, package_id, org_id, status, stripe_subscription_id)
values (10000012, 'wert', '2023-10-02 22:19:50.321129', '2023-10-02 00:00:00', '2023-11-02 00:00:00', 3455.00, 99004, 99004, 'active', null);
