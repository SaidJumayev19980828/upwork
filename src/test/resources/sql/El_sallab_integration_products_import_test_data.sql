--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'el-sallab', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());


--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(201, 'CERAMIC'		, now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES(202, 'COLLECTION'	, now(), now());



--tags                                                                                                                                      
INSERT INTO public.tags (category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(201, 'CERMAICS'		,'CERMAICS'		, 'ceramics', '{}', 0, 99001);
INSERT INTO public.tags (category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(202, 'ROYAL'		,'ROYAL'		, 'royal', '{}', 0, 99001);




--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'GEMMA', now(), now(), 99001);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'lecico', now(), now(), 99001);


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
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Color', 'color', 'whatever', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Size', 'size', 'bla bla bla', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Class', 'class', 'Material of the shoes', 99001);


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());


-- variants for each product
insert into public.product_variants(id, "name" , product_id, barcode, feature_spec ) values(310001, 'var' 	, 1001, 'ABCD1234', '{"234":"40", "235":"pink"}');


--inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(60001, 50001, 6, now(), now(), 99002, 600.0, 310001);


-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67006, 'PRODUCT_VARIANT');



-- insert integration mappings
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67005, '501', 'Cust Trans', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67005, '502', 'DLalmaza', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67006, '310001', '11CYM-0010001', 99001);




-- Mandatory integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6601, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6602, 'MAX_REQUESTS_PER_SECOND', TRUE);