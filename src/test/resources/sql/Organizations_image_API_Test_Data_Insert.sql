INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());

INSERT INTO public.files(id, organization_id, url, location, mimetype, orig_filename)
    VALUES(901,99002, '99002/nasnav--test-photo.png', '99002/nasnav--test-photo.png', 'image/png', 'nasnav--Test_Photo.png');

INSERT INTO public.organization_images(id, organization_id, uri) VALUES (901,99002, '99002/nasnav--test-photo.png');

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--inserting users
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, '123',  502);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (69, now(), now(), 'testuser2@nasnav.com', 99002, '456',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (70, now(), now(), 'testuser3@nasnav.com', 99001, '789',  502);

--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 2, now(), now());