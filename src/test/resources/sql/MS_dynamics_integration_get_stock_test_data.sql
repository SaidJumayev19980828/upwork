--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());


--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(201, 'PERFUMES', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(202, 'SKIN CARE', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(203, 'MAKE UP', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(204, 'FRAGRANCE', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(205, 'BC POSM', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(206, 'BABY CARE', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(207, 'SC POSM', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(208, 'FOOT CARE', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(209, 'FR POSM', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(210, 'DIM', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(211, 'TREATMENT', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(212, 'FOOTC POSM', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(213, 'MAKE-UP AC', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(214, 'FORTUNE', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(215, 'HAIR CARE', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(217, 'READY TO WEAR', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(218, 'MU POSM', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(219, 'BODY', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(220, 'DELSEY', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(221, 'ACCESSORY', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(222, 'GIFT VOUCHER', now(), now());




--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'Baldman', now(), now(), 99001);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);


--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (50001, 'shop_1', 102, now(), now(), 99001);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (50002, 'shop_2', 101, now(), now(), 99001);



--inserting Employee Users
INSERT INTO public.employee_users(id, name, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', now(), now(), 'testuser1@nasnav.com', 99001, 'abcdefg',  50001);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, 'hijkllm',  50001);



--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'ORGANIZATION_MANAGER', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(6, 'STORE_MANAGER', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(7, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(8, 'CUSTOMER', now(), now(), 99001);



--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 69, 3, now(), now());



--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Lispstick Color', 'lipstick_color', 'whatever', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Lipstick flavour', 'lipstick_flavour', 'bla bla bla', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe material', 's-material', 'Material of the shoes', 99001);


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());


-- variants for each product
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec ) values(310001, 'var' 	, 1001, '6221105441060', '{"234":"40", "235":"pink"}');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec ) values(310002, 'var 2' , 1001, '6221105441061', '{"234":"80", "235":"Blue"}');


--inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(60001, 50001, 6, now(), now(), 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(60002, 50002, 6, now(), now(), 99001, 600.0, 310002);


-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67006, 'PRODUCT_VARIANT');



-- insert integration mappings
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67005, '50001', 'FOarabia', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67006, '310001', '11CYM-0010001', 99001);




-- Mandatory integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6601, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6602, 'MAX_REQUESTS_PER_SECOND', TRUE);