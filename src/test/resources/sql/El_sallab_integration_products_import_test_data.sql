--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'el-sallab');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);


--inserting categories
INSERT INTO public.categories(id, name) VALUES(201, 'CERAMIC'		);
INSERT INTO public.categories(id, name) VALUES(202, 'COLLECTION'	);



--tags                                                                                                                                      
INSERT INTO public.tags (category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(201, 'CERAMIC'		,'CERMAIC'		, 'ceramics', '{}', 0, 99001);
INSERT INTO public.tags (category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(202, 'ROYAL'		,'ROYAL'		, 'royal', '{}', 0, 99001);
INSERT INTO public.tags (category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(202, 'VANDOME'		,'VANDOME'		, 'vandome', '{}', 0, 99001);
INSERT INTO public.tags (category_id, "name", alias, p_name, metadata, removed, organization_id) VALUES(201, 'FLOOR'		,'FLOOR'		, 'floor', '{}', 0, 99001);



--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (111102, 201, 'lecico', 99001);


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (50001, 'shop_1', 111102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (50002, 'shop_2', 111102, 99001, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (50003, 'shop_3', 111102, 99001, 0);



--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  50001);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  50001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);

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
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 3);



--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Color', 'color', 'whatever', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Size', 'size', 'bla bla bla', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Class', 'class', 'Class of ceramic', 99001);




-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67006, 'PRODUCT_VARIANT');



-- insert integration mappings
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67005, '50002', '183', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) VALUES(67005, '50003', '110', 99001);




-- Mandatory integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6601, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6602, 'MAX_REQUESTS_PER_SECOND', TRUE);