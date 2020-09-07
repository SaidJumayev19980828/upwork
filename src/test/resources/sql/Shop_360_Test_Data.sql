INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');

INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99002);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99002, 0);

INSERT INTO public.shop360s(id, shop_id) VALUES(10010, 501);
INSERT INTO public.shop360s(id, shop_id) VALUES(10011, 502);

INSERT INTO public.shop_floors(id, number, name, shop360_id, organization_id) VALUES(100011, 1, 'floor_1', 10010, 99001);
INSERT INTO public.shop_floors(id, number, name, shop360_id, organization_id) VALUES(100012, 1, 'floor_2', 10011, 99002);

INSERT INTO public.shop_sections(id, shop_floor_id, organization_id, name) VALUES(100011, 100011, 99001, 'section1');
INSERT INTO public.shop_sections(id, shop_floor_id, organization_id, name) VALUES(100012, 100012, 99002, 'section2');

INSERT INTO public.scenes(id, shop_section_id, organization_id, name) VALUES(100011, 100011, 99001, 'scene_1');
INSERT INTO public.scenes(id, shop_section_id, organization_id, name) VALUES(100012, 100012, 99002, 'scene_2');

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99002, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99002, '161718',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99002, '192021',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '222324', now(), 158, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99002);
insert into roles(id, name,  organization_id) values(5, 'STORE_MANAGER', 99002);
insert into roles(id, name,  organization_id) values(3, 'STORE_EMPLOYEE', 99002);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 5);

INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at)
    VALUES (1001, 'product_1', 'product-one',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, product_type)
    VALUES (1002, 'product_2', 'product-two',101, 201, 99001, now(), now(), 2);
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at)
    VALUES (1003, 'product_3', 'product-3',101, 201, 99001, now(), now());

INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, product_type)
    VALUES (1004, 'product_4', 'product-4',102, 201, 99002, now(), now(), 2);
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at)
    VALUES (1005, 'product_5', 'product-5',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at)
    VALUES (1006, 'product_6', 'product-6',102, 201, 99002, now(), now());

INSERT INTO public.shop360_products(id, product_id, shop_id, floor_id, scene_id, section_id, published)
    values (10001, 1001, 501, 100011, 100011, 100011, 1);
INSERT INTO public.shop360_products(id, product_id, shop_id, floor_id, scene_id, section_id, published)
    values (10002, 1002, 501, 100011, 100011, 100011, 1);

INSERT INTO public.shop360_products(id, product_id, shop_id, floor_id, scene_id, section_id, published)
    values (10003, 1003, 501, 100011, 100011, 100011, 2);
INSERT INTO public.shop360_products(id, product_id, shop_id, floor_id, scene_id, section_id, published)
    values (10004, 1004, 501, 100011, 100011, 100011, 2);


