--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'fortune');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting countries/ cities/ areas/ sub-areas
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id, country_id, name) values(100001, 1,'Cairo');
INSERT INTO public.areas(id, name, city_id) values(100001, 'new cairo', 100001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888001, 100001, 'Badr city', 99001);

--inserting organization domain
INSERT INTO public.organization_domains (id, "domain", organization_id) VALUES(1, 'fortune.nasnav.com', 99001);
INSERT INTO public.organization_domains (id, "domain", organization_id) VALUES(2, 'fortune-egypt.com', 99001);
INSERT INTO public.organization_domains (id, "domain", organization_id, subdir) VALUES(3, 'nasnav.com', 99001, 'fortune');

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
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser4@nasnav.com', 99001, 'eereeee',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '123456', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, 'eereeee', now(), 71, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(3, 'STORE_MANAGER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 2);

INSERT INTO public.products(id, name, brand_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',104, 99001, now(), now());


INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters, id)VALUES('TEST', 99001, '{"hotline":"19888"}', 11001);


--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com','user2','456', 99002);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '456', now(), null, 89);

INSERT INTO public.addresses(id, address_line_1, sub_area_id) values(12300003, 'address line', 888001);
INSERT INTO public.User_addresses(id, user_id ,address_id ,principal ) values(12300003, 88, 12300003, false);