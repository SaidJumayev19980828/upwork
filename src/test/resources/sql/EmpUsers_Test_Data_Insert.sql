--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (501, 'shop_1', 102, 99002);
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (502, 'shop_2', 101, 99001);

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99001, '123',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99001, '456',  501);
	INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (72, 'Magdy', 'testuser6@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (73, 'Shafiq', 'testuser7@nasnav.com', 99001, '161718',  501);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (74, 'Mohmyo', 'testuser8@nasnav.com', 99001, '192021',  501);

INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (75, 'Said', 'testuser11@nasnav.com', 99002, '222324',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (76, 'testuser21@nasnav.com', 99002, '252627',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (77, 'testuser41@nasnav.com', 99002, '282930',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (78, 'testuser51@nasnav.com', 99002, '313233',  502);
	INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (79, 'Kira', 'testuser61@nasnav.com', 99002, '343536',502);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (80, 'L', 'testuser71@nasnav.com', 99002, '373839',  502);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (81, 'Hussien', 'testuser81@nasnav.com', 99002, '404142',  502);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'Walid', 'testuser3@nasnav.com', 99001, 'nopqrst',  502);
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (159, 'Walid', 'user1@nasnav.com', 99001, 'nopqrst',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

--inserting Users
INSERT INTO public.users(id, email,  user_name, authentication_token, address, country, city, phone_number, image, organization_id, encrypted_password)
    VALUES (88, 'user1@nasnav.com','user1','yuhjhu', '21 jump street', 'Egypt', 'Cairo', '+021092154875','/urls/images/fdsafag23.jpg',  99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(3, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(7, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(8, 'CUSTOMER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 72, 5);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (25, 73, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (26, 74, 7);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (27, 75, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (28, 76, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (29, 77, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (30, 78, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (31, 79, 5);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (32, 80, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (33, 81, 7);