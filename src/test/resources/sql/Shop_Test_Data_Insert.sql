
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (503, 'shop_3', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (504, 'shop_4', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (505, 'shop_5', 102, 99001, 0);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, '123', now(), null, 88);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '222324', now(), 158, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(3, 'STORE_EMPLOYEE', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 5);

INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at)
    VALUES (1001, 'product_1', 'product-one',101, 201, 99001, now(), now());

insert into public.product_variants(id, "name" , product_id ) values(310001, 'var', 1001);

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id)
    values(601, 503, 3, 99001, 600.0, 310001);

insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id)
    values(330033, 88, now(), now(), 99001, 0, 504);

INSERT INTO public.shop360s(id, shop_id) VALUES (1001, 505);