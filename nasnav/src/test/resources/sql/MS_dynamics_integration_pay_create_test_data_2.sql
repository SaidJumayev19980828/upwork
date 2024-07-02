INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);


--inserting categories
INSERT INTO public.categories(id, name) VALUES(201, 'PERFUMES');
INSERT INTO public.categories(id, name) VALUES(202, 'SKIN CARE');
INSERT INTO public.categories(id, name) VALUES(203, 'MAKE UP');
INSERT INTO public.categories(id, name) VALUES(204, 'FRAGRANCE');
INSERT INTO public.categories(id, name) VALUES(205, 'BC POSM');
INSERT INTO public.categories(id, name) VALUES(206, 'BABY CARE');
INSERT INTO public.categories(id, name) VALUES(207, 'SC POSM');
INSERT INTO public.categories(id, name) VALUES(208, 'FOOT CARE');
INSERT INTO public.categories(id, name) VALUES(209, 'FR POSM');
INSERT INTO public.categories(id, name) VALUES(210, 'DIM');
INSERT INTO public.categories(id, name) VALUES(211, 'TREATMENT');
INSERT INTO public.categories(id, name) VALUES(212, 'FOOTC POSM');
INSERT INTO public.categories(id, name) VALUES(213, 'MAKE-UP AC');
INSERT INTO public.categories(id, name) VALUES(214, 'FORTUNE');
INSERT INTO public.categories(id, name) VALUES(215, 'HAIR CARE');
INSERT INTO public.categories(id, name) VALUES(217, 'READY TO WEAR');
INSERT INTO public.categories(id, name) VALUES(218, 'MU POSM');
INSERT INTO public.categories(id, name) VALUES(219, 'BODY');
INSERT INTO public.categories(id, name) VALUES(220, 'DELSEY');
INSERT INTO public.categories(id, name) VALUES(221, 'ACCESSORY');
INSERT INTO public.categories(id, name) VALUES(222, 'GIFT VOUCHER');




--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'Baldman', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (50001, 'shop_1', 102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (50002, 'shop_2', 101, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (55555, 'shop_3', 101, 99001, 0);



--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  50001);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  50001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), null, 68);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), null, 69);

INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123eerd', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123eerd', now(), null, 88);


--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99001);
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
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 3);



--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Lispstick Color', 'lipstick_color', 'whatever', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Lipstick flavour', 'lipstick_flavour', 'bla bla bla', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe material', 's-material', 'Material of the shoes', 99001);


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());


-- variants for each product
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec ) values(310001, 'var' 	, 1001, '6221105441060', '{"234":"40", "235":"pink"}');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec ) values(310002, 'var 2' , 1001, '6221105441061', '{"234":"80", "235":"Blue"}');
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec ) values(310003, 'invalid var' , 1001, '6221105441063', '{"234":"90", "235":"Blue"}');


--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(60001, 50001, 6, 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(60002, 50001, 55, 99001, 600.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(60003, 50002, 66, 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(60004, 55555, 88, 99001, 600.0, 310003);


--insert orders
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id, payment_status, address) values(430033, 88, now(), now(), 99001, 0, 50001, 0, 'a better place');


-- insert order items
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(430033, 60001, 3, 600.0, 1);

-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67006, 'PRODUCT_VARIANT');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67007, 'ORDER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67008, 'PAYMENT');


-- insert integration mappings
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67005, '50001', 'FOarabia', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67005, '55555', 'Delsey', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67006, '310001', '11CYM-0010001', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67006, '310003', '11CYM-0015551', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67004, '88', 'UNR-023538', 99001);



-- Mandatory integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6601, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6602, 'MAX_REQUESTS_PER_SECOND', TRUE);