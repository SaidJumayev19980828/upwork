INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');
INSERT INTO public.organizations(id, name) VALUES (99003, 'organization_3');


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
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser3@nasnav.com', 99001, 'sfeesdfsdf',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, 'sfeesdfsdf', now(), 70, null);

--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);


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




