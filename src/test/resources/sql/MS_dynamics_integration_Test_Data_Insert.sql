--inserting organizations(already inserted before the test class is loaded)
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

INSERT INTO public.emp_user_tokens(id, token, update_time, user_id) VALUES (1, 'abcdefg', now(), 68);
INSERT INTO public.emp_user_tokens(id, token, update_time, user_id) VALUES (2, 'hijkllm', now(), 69);

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

-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67006, 'PRODUCT_VARIANT');


-- Mandatory integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6601, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6602, 'MAX_REQUESTS_PER_SECOND', TRUE);