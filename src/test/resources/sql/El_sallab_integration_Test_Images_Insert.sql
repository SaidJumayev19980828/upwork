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



--inserting products
INSERT INTO public.products(id, name, brand_id, organization_id, created_at, updated_at) VALUES (1001, 'VANDOME',101, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id,  organization_id, created_at, updated_at) VALUES (1002, 'DYNASTY',101, 99001, now(), now());



-- variants for each product
insert into public.product_variants(id, "name" , product_id , barcode) values(310001, 'var' 	, 1001 , '0550500023100011');
insert into public.product_variants(id, "name" , product_id , barcode) values(310002, 'var' 	, 1001 , '0550500023100012');
insert into public.product_variants(id, "name" , product_id , barcode) values(310003, 'var' 	, 1001 , '0550500023101503');
insert into public.product_variants(id, "name" , product_id , barcode) values(310004, 'var' 	, 1002 , '0550500023101111');
insert into public.product_variants(id, "name" , product_id , barcode) values(310005, 'var' 	, 1002 , '0550500023101212');



--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 6, 99001, 600.00, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 501, 8, 99001, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 501, 4, 99001, 200.00, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 502, 6, 99001, 700.00, 310004);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(605, 502, 6, 99001, 780.00, 310005);
