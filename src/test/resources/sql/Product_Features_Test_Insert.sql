----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(501 , 'Bundle Shop'  , 99002);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(502 , 'another Shop'  , 99001);



--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com','user3','789', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '456', now(), null, 89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '789', now(), null, 90);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (7, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (8, '222324', now(), 158, null);

--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'STORE_EMPLOYEE', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 71, 5);


--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Shoe size', 's-size', 'Size of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Shoe color', 's-color', 'Color of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe size', 's-size', 'Size of the shoes', 99002);


-- insert products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());


-- variants for each product
INSERT INTO public.product_variants(id,product_id, feature_spec, name, p_name, description, barcode)
VALUES(80001,1001, '{"235": "white"}', 'orginal variant', 'orginal_variant', 'has the feature', 'BCF559354');
INSERT INTO public.product_variants(id,product_id, feature_spec, name, p_name, description, barcode, removed)
VALUES(80002,1001, '{"234": "45"}', 'orginal variant', 'orginal_variant', 'removed', 'AFDSSDF214', 1);


--inserting product features
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 6, 99001, 600.0, 80001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 501, 8, 99001, 1200.0, 80002);

