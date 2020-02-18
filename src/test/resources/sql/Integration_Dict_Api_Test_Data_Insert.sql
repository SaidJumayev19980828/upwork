INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99003, 'organization_3', now(), now());


--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--inserting Employee Users
INSERT INTO public.employee_users(id, name, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', now(), now(), 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (70, now(), now(), 'testuser3@nasnav.com', 99001, 'sfeesdfsdf',  501);

--inserting Roles
insert into public.roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 4, now(), now());


-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67001, 'PRODUCT_VARIANT');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67002, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67003, 'ORDER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'PAYMENT');


-- insert integration mapping
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) 
	VALUES(67001, '444', 'ABC444', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) 
	VALUES(67001, '123', 'ABC123', 99001) ;
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id)
	VALUES(67001, '1234', 'ABC1234', 99001) ;
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) 
	VALUES(67001, '456', 'ABC465', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) 
	VALUES(67001, '789', 'ABC789', 99001) ;
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id)
	VALUES(67001, '888', 'ABC888', 99001) ;
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id)
	VALUES(67005, '999', 'ABC999', 99001) ;
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id)
	VALUES(67004, '310001', '5', 99002) ;




