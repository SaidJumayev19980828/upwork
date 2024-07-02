INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99002);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (51, 'shop_1', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (52, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (53, 'shop_3', 101, 99002, 0);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  52);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  51);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
	VALUES (70, 'testuser4@nasnav.com', 99002, '161718');
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  52);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99001, '222324',  51);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '222324', now(), 158, null);



--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99002);
insert into roles(id, name,  organization_id) values(5, 'STORE_MANAGER', 99002);
insert into roles(id, name,  organization_id) values(3, 'STORE_EMPLOYEE', 99002);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 5);

INSERT INTO public.users(id, email, organization_id, authentication_token) VALUES (81, 'user81@nasnav.com', 99001, 'user81');
INSERT INTO public.users(id, email, organization_id, authentication_token) VALUES (82, 'user82@nasnav.com', 99002, 'user82');

INSERT INTO public.user_tokens(id, token, update_time, user_id) VALUES (6, 'user81', now(), 81);
INSERT INTO public.user_tokens(id, token, update_time, user_id) VALUES (7, 'user82', now(), 82);

INSERT INTO public.room_templates(id, shop_id, scene_id, data) VALUES (501, 51, 'anything501', '{"property501": "value501"}');
INSERT INTO public.room_templates(id, shop_id, scene_id, data) VALUES (502, 52, 'anything502', '{"property502": "value502"}');

INSERT INTO public.room_sessions(id, template_id, external_id, created_at, user_creator) VALUES (5001, 501, 'external5001', now(), 81);
