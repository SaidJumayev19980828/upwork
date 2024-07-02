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

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser3@nasnav.com', 99002, '123456',  501);

INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (71, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (72, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (73, 'user3@nasnav.com','user3','789', 99002);

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

INSERT INTO public.products(id, name, brand_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',104, 99001, now(), now());



--INSERT countries
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);


INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (101, 'GET /API/TEST/LOG_1', '2022-07-01', null, 68, 99001);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (102, 'GET /API/TEST/LOG_1', '2022-07-02', 72, null, 99002);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (103, 'GET /API/TEST/LOG_2', '2022-07-02', null, 69, 99002);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (104, 'GET /API/TEST/LOG_3', '2022-07-03', 71, null, 99001);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (105, 'GET /API/TEST/LOG_3', '2022-07-04', 72, null, 99002);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (106, 'GET /API/TEST/LOG_4', '2022-07-05', 72, null, 99002);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (107, 'GET /API/TEST/LOG_4', '2022-07-06', null, 68, 99001);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (108, 'GET /API/TEST/LOG_3', '2022-07-07', 71, null, 99001);
INSERT INTO public.api_logs (id, url, call_date, customer_id, employee_id, organization_id) VALUES (109, 'GET /API/TEST/LOG_3', '2022-07-08', 73, null, 99002);
