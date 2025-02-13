
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_3', 101, 99001, 0);

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  502, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (70, 'testuser4@nasnav.com', 99001, '123',  502, 201);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (71, 'testuser5@nasnav.com', 99001, '456',  502, 201);
	INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (72, 'Magdy', 'testuser6@nasnav.com', 99001, '131415',  502);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (73, 'Shafiq', 'testuser7@nasnav.com', 99001, '161718',  502);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (74, 'Mohmyo', 'testuser8@nasnav.com', 99001, '192021',  502);

INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (75, 'Said', 'testuser11@nasnav.com', 99002, '222324',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (76, 'testuser21@nasnav.com', 99002, '252627',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (77, 'testuser41@nasnav.com', 99002, '282930',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (78, 'testuser51@nasnav.com', 99002, '313233',  501);
	INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (79, 'Kira', 'testuser61@nasnav.com', 99002, '343536',501);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (80, 'L', 'testuser71@nasnav.com', 99002, '373839',  501);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (81, 'Hussien', 'testuser81@nasnav.com', 99002, '404142',  501);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'Walid', 'testuser3@nasnav.com', 99001, 'nopqrst',  502);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (159, 'Walid', 'user1@nasnav.com', 99001, 'nopqrstt',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id, user_status)
	VALUES (160, 'testuser22@nasnav.com', 99001, 'hhhkkk',  502, 201);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1001, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1002, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1003, '123', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1004, '456', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1005, '131415', now(), 72, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1006, '161718', now(), 73, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1007, '192021', now(), 74, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1008, '222324', now(), 75, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1009, '252627', now(), 76, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1010, '282930', now(), 77, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1011, '313233', now(), 78, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1012, '343536', now(), 79, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1013, '373839', now(), 80, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1014, '404142', now(), 81, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1015, 'nopqrst', now(), 158, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1016, 'uvwxyz', now(), 159, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1017, 'hhhkkk', now(), 160, null);

--inserting Users
INSERT INTO public.users(id, email,  user_name, authentication_token, phone_number, image, organization_id, encrypted_password)
    VALUES (88, 'user1@nasnav.com','user1','yuhjhu', '+021092154875','/urls/images/fdsafag23.jpg',  99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, 'yuhjhu', now(), null, 88);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(3, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(7, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(8, 'CUSTOMER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99920, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99921, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99922, 70, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99923, 71, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99924, 72, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99925, 73, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99926, 74, 7);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99927, 75, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99928, 76, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99929, 77, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99930, 78, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99931, 79, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99932, 80, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99933, 81, 7);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (99934, 160, 4);